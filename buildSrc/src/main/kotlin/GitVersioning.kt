import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

abstract class PythonScriptValueSource : ValueSource<String, PythonScriptValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        val scriptPath: Property<String>
        val workingDir: Property<File>
    }

    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): String {
        val scriptFile = File(parameters.scriptPath.get())
        if (!scriptFile.exists()) return ""
        val output = ByteArrayOutputStream()
        return try {
            val execResult = execOperations.exec {
                commandLine("python3", scriptFile.absolutePath)
                workingDir = parameters.workingDir.get()
                standardOutput = output
                isIgnoreExitValue = true
            }
            if (execResult.exitValue == 0) output.toString().trim() else ""
        } catch (_: Exception) {
            ""
        }
    }
}

abstract class GitCommandValueSource : ValueSource<String, GitCommandValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        val command: ListProperty<String>
        val rootDir: Property<File>
    }

    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): String {
        val output = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()
        return try {
            val execResult = execOperations.exec {
                commandLine(parameters.command.get())
                workingDir = parameters.rootDir.get()
                standardOutput = output
                errorOutput = errorStream
                isIgnoreExitValue = true
            }
            if (execResult.exitValue == 0) {
                output.toString().trim()
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        }
    }
}

object GitVersioning {
    /**
     * Base version code offset - used only as fallback when Google Play API is unavailable.
     * 
     * Historical context:
     * - The repository underwent a major restructure/reset, drastically reducing commit count
     * - The last deployed version code in Google Play: 367 (as of 2026-02-19)
     * - Current git commit count: 2
     * - This offset (367 - 2 = 365) ensures version codes remain monotonically increasing
     *   and consistent with Google Play's expectations.
     * 
     * Previous offset history:
     * - Initial offset: 25 (calculated as 348 - 323)
     * - This was based on Play Store version 348 and git count 323
     * - After repository restructure, the offset became outdated
     * 
     * **Primary Method**: Fetch from Google Play API (see fetchFromGooglePlay())
     * **Fallback Method**: commit_count + BASE_VERSION_CODE_OFFSET
     * 
     * Note: This fallback offset must be updated if Play Store version changes independently.
     */
    private const val BASE_VERSION_CODE_OFFSET = 365
    
    @JvmStatic
    fun getVersionCode(project: Project): Int {
        // Primary method: Fetch from Google Play Store API
        // This ensures version codes are always accurate regardless of git history
        val playStoreVersion = fetchFromGooglePlay(project)
        if (playStoreVersion != null) {
            project.logger.lifecycle("Using Google Play API version: $playStoreVersion")
            return playStoreVersion
        }
        
        // Fallback: Use git commit count + offset
        // This runs when API is unavailable (no credentials, network issues, etc.)
        project.logger.warn("Google Play API unavailable, falling back to git-based versioning")
        return getGitBasedVersionCode(project)
    }
    
    /**
     * Fetch the latest version code from Google Play Store using the Python script.
     * Returns the next version code to use (latest + 1).
     * 
     * @return Next version code, or null if fetch fails
     */
    private fun fetchFromGooglePlay(project: Project): Int? {
        val scriptPath = File(project.rootDir, "scripts/fetch_play_version.py")
        if (!scriptPath.exists()) {
            project.logger.debug("Play Store fetch script not found: ${scriptPath.absolutePath}")
            return null
        }
        
        val credentialsPath = File(project.rootDir, "service-account.json")
        if (!credentialsPath.exists()) {
            project.logger.info("Service account credentials not found: ${credentialsPath.absolutePath}")
            return null
        }
        
        return try {
            val outputProvider = project.providers.of(PythonScriptValueSource::class.java) {
                parameters.scriptPath.set(scriptPath.absolutePath)
                parameters.workingDir.set(project.rootDir)
            }
            val output = outputProvider.get()
            if (output.isEmpty()) return null

            val versionCode = output.toIntOrNull()
            if (versionCode != null && versionCode > 0) {
                project.logger.lifecycle("Fetched Play Store version: ${versionCode - 1}, using: $versionCode")
                versionCode
            } else {
                project.logger.warn("Play Store fetch script returned unexpected output: '$output'")
                null
            }
        } catch (e: Exception) {
            project.logger.warn("Failed to fetch from Google Play: ${e.message}")
            null
        }
    }
    
    /**
     * Fallback method: Calculate version code from git commit count.
     * Used when Google Play API is unavailable.
     */
    private fun getGitBasedVersionCode(project: Project): Int {
        // Note: Requires full Git history (not a shallow clone)
        // GitHub Actions workflows should use fetch-depth: 0
        val countProvider = gitCommand(project, listOf("git", "rev-list", "--count", "HEAD"))
        val count = countProvider.orNull?.toIntOrNull()
        if (count == null) {
            project.logger.warn("Failed to get Git commit count, falling back to version code 1. " +
                "Ensure repository is not a shallow clone.")
            return 1
        }
        
        // Calculate version code: commit count + base offset
        // Base offset maintains consistency with historical Google Play versions
        val versionCode = count + BASE_VERSION_CODE_OFFSET
        
        val branchProvider = gitCommand(project, listOf("git", "rev-parse", "--abbrev-ref", "HEAD"))
        val branch = branchProvider.orNull?.trim() ?: "unknown"
        project.logger.lifecycle("Branch: $branch, Fallback Version Code: $versionCode")
        
        return versionCode
    }

    @JvmStatic
    fun getVersionName(project: Project): String {
        // When FORCE_DEV_VERSION is set (e.g. manual workflow_dispatch trigger), always
        // produce a dev version name regardless of branch or tag state.
        val forceDevVersion = System.getenv("FORCE_DEV_VERSION") == "true"

        // Get current branch
        val branchProvider = gitCommand(project, listOf("git", "rev-parse", "--abbrev-ref", "HEAD"))
        val branch = branchProvider.orNull?.trim() ?: ""
        
        // Try to get the latest tag
        val tagProvider = gitCommand(project, listOf("git", "describe", "--tags", "--abbrev=0"))
        val tag = tagProvider.orNull?.trim() ?: ""

        if (tag.isNotEmpty()) {
            // Check if current commit is tagged
            val currentCommitProvider = gitCommand(project, listOf("git", "rev-parse", "HEAD"))
            val currentCommit = currentCommitProvider.orNull?.trim() ?: ""
            val tagCommitProvider = gitCommand(project, listOf("git", "rev-list", "-n", "1", tag))
            val tagCommit = tagCommitProvider.orNull?.trim() ?: ""

            if (!forceDevVersion && currentCommit == tagCommit && currentCommit.isNotEmpty()) {
                // Clean release build (tagged commit, automatic trigger)
                return tag.removePrefix("v")
            } else if (!forceDevVersion && (branch == "master" || branch == "main")) {
                // Master/main branch: use clean version without -dev suffix
                // This ensures production deployments from master have proper version names
                return tag.removePrefix("v")
            } else {
                // Development build with commit hash (for feature branches or manual triggers)
                val shortHashProvider = gitCommand(project, listOf("git", "rev-parse", "--short", "HEAD"))
                val shortHash = shortHashProvider.orNull?.trim() ?: "unknown"
                val commitsSinceTagProvider = gitCommand(project, listOf("git", "rev-list", "$tag..HEAD", "--count"))
                val commitsSinceTag = commitsSinceTagProvider.orNull?.trim() ?: "0"
                return "${tag.removePrefix("v")}-dev.$commitsSinceTag+$shortHash"
            }
        }
        
        // No tags found
        if (!forceDevVersion && (branch == "master" || branch == "main")) {
            // Master/main branch: use simple version without -dev suffix
            return "1.0.0"
        } else {
            // Development branch or manual trigger: use version with -dev suffix and commit hash
            val shortHashProvider = gitCommand(project, listOf("git", "rev-parse", "--short", "HEAD"))
            val shortHash = shortHashProvider.orNull?.trim() ?: "unknown"
            return "0.0.1-dev+$shortHash"
        }
    }

    private fun gitCommand(project: Project, command: List<String>): Provider<String> {
        return project.providers.of(GitCommandValueSource::class.java) {
            parameters.command.set(command)
            parameters.rootDir.set(project.rootDir)
        }
    }
}
