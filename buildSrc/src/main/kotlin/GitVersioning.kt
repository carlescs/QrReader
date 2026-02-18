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
     * Base version code offset to maintain consistency with Google Play.
     * 
     * Historical context:
     * - The repository was restructured at some point, causing a mismatch between
     *   the git commit count and the actual version codes in Google Play.
     * - The last known version code in Google Play was 348.
     * - The git commit count at that time was 323.
     * - This offset (348 - 323 = 25) ensures version codes remain monotonically increasing
     *   and consistent with Google Play's expectations.
     * 
     * This offset is added to all version code calculations to ensure:
     * 1. No conflicts with existing Google Play versions
     * 2. Monotonically increasing version codes
     * 3. Consistent versioning going forward
     * 
     * NOTE: This is now used only as a fallback when Google Play API fetch fails.
     */
    private const val BASE_VERSION_CODE_OFFSET = 25
    
    @JvmStatic
    fun getVersionCode(project: Project): Int {
        // Try to fetch version code from Google Play first
        val playStoreVersion = fetchFromGooglePlay(project)
        if (playStoreVersion != null) {
            project.logger.lifecycle("Using Google Play version code: $playStoreVersion")
            return playStoreVersion
        }
        
        // Fallback to git-based versioning
        project.logger.warn("Google Play API fetch failed, falling back to git-based versioning")
        return getGitBasedVersionCode(project)
    }
    
    /**
     * Fetches the latest version code from Google Play and returns the next version code (latest + 1).
     * 
     * Requirements:
     * - Python 3 must be available
     * - service-account.json must exist in the project root
     * - Google API packages must be installed (google-api-python-client, google-auth)
     * 
     * @return The next version code to use, or null if the fetch failed
     */
    private fun fetchFromGooglePlay(project: Project): Int? {
        val scriptPath = File(project.rootDir, "scripts/fetch_play_version.py")
        val credentialsPath = File(project.rootDir, "service-account.json")
        
        // Check if required files exist
        if (!scriptPath.exists()) {
            project.logger.debug("Google Play version script not found at: ${scriptPath.absolutePath}")
            return null
        }
        
        if (!credentialsPath.exists()) {
            project.logger.debug("Service account credentials not found at: ${credentialsPath.absolutePath}")
            return null
        }
        
        return try {
            val output = ByteArrayOutputStream()
            val errorStream = ByteArrayOutputStream()
            
            val execResult = project.exec {
                commandLine("python3", scriptPath.absolutePath)
                workingDir = project.rootDir
                standardOutput = output
                errorOutput = errorStream
                isIgnoreExitValue = true
            }
            
            if (execResult.exitValue == 0) {
                val versionCode = output.toString().trim().toIntOrNull()
                if (versionCode != null && versionCode > 0) {
                    project.logger.lifecycle("Successfully fetched version code from Google Play: $versionCode")
                    versionCode
                } else {
                    project.logger.warn("Invalid version code from Google Play: ${output.toString().trim()}")
                    project.logger.debug("Error output: ${errorStream.toString()}")
                    null
                }
            } else {
                project.logger.warn("Failed to fetch version code from Google Play (exit code: ${execResult.exitValue})")
                project.logger.debug("Error output: ${errorStream.toString()}")
                null
            }
        } catch (e: Exception) {
            project.logger.warn("Exception while fetching version code from Google Play: ${e.message}")
            null
        }
    }
    
    /**
     * Git-based version code calculation (fallback method).
     * This is used when Google Play API fetch fails.
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
        
        // For feature branches, add a branch-specific offset to prevent version code collisions
        // when multiple feature branches are deployed to the alpha track in parallel
        val branchProvider = gitCommand(project, listOf("git", "rev-parse", "--abbrev-ref", "HEAD"))
        val branch = branchProvider.orNull?.trim() ?: "master"
        
        // Calculate the base version code with the historical offset
        val baseVersionCode = count + BASE_VERSION_CODE_OFFSET
        
        // Only apply branch offset for non-master/main branches
        if (branch != "master" && branch != "main" && branch != "HEAD") {
            // Generate a consistent hash from the branch name
            // Handle Int.MIN_VALUE edge case by converting to Long first
            val branchHash = (kotlin.math.abs(branch.hashCode().toLong()) % 10000).toInt()
            project.logger.lifecycle("Feature branch detected: $branch, adding offset: $branchHash")
            // Add offset to create unique version code for this branch
            // The offset is multiplied by 100000 to ensure it doesn't conflict with commit count
            return baseVersionCode + (branchHash * 100000)
        }
        
        return baseVersionCode
    }

    @JvmStatic
    fun getVersionName(project: Project): String {
        // Try to get the latest tag
        val tagProvider = gitCommand(project, listOf("git", "describe", "--tags", "--abbrev=0"))
        val tag = tagProvider.orNull?.trim() ?: ""

        if (tag.isNotEmpty()) {
            // Check if current commit is tagged
            val currentCommitProvider = gitCommand(project, listOf("git", "rev-parse", "HEAD"))
            val currentCommit = currentCommitProvider.orNull?.trim() ?: ""
            val tagCommitProvider = gitCommand(project, listOf("git", "rev-list", "-n", "1", tag))
            val tagCommit = tagCommitProvider.orNull?.trim() ?: ""

            if (currentCommit == tagCommit && currentCommit.isNotEmpty()) {
                // Clean release build
                return tag.removePrefix("v")
            } else {
                // Development build with commit hash
                val shortHashProvider = gitCommand(project, listOf("git", "rev-parse", "--short", "HEAD"))
                val shortHash = shortHashProvider.orNull?.trim() ?: "unknown"
                val commitsSinceTagProvider = gitCommand(project, listOf("git", "rev-list", "$tag..HEAD", "--count"))
                val commitsSinceTag = commitsSinceTagProvider.orNull?.trim() ?: "0"
                return "${tag.removePrefix("v")}-dev.$commitsSinceTag+$shortHash"
            }
        }
        
        // No tags found, use default
        val shortHashProvider = gitCommand(project, listOf("git", "rev-parse", "--short", "HEAD"))
        val shortHash = shortHashProvider.orNull?.trim() ?: "unknown"
        return "0.0.1-dev+$shortHash"
    }

    private fun gitCommand(project: Project, command: List<String>): Provider<String> {
        return project.providers.of(GitCommandValueSource::class.java) {
            parameters.command.set(command)
            parameters.rootDir.set(project.rootDir)
        }
    }
}
