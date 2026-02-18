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
     */
    private const val BASE_VERSION_CODE_OFFSET = 25
    
    @JvmStatic
    fun getVersionCode(project: Project): Int {
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
        //
        // Note: Branch-specific offsets were removed to prevent extremely large version codes.
        // Previously, feature branches would get offsets like 891,400,000 which approached
        // Int.MAX_VALUE (2,147,483,647) and made debugging difficult.
        //
        // For parallel feature branch deployments to Alpha track, teams should:
        // - Deploy feature branches sequentially (recommended)
        // - Use different Google Play tracks (Internal Testing, Alpha, Beta)
        // - Manually coordinate version codes if truly needed
        val versionCode = count + BASE_VERSION_CODE_OFFSET
        
        val branchProvider = gitCommand(project, listOf("git", "rev-parse", "--abbrev-ref", "HEAD"))
        val branch = branchProvider.orNull?.trim() ?: "unknown"
        project.logger.lifecycle("Branch: $branch, Version Code: $versionCode")
        
        return versionCode
    }

    @JvmStatic
    fun getVersionName(project: Project): String {
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

            if (currentCommit == tagCommit && currentCommit.isNotEmpty()) {
                // Clean release build (tagged commit)
                return tag.removePrefix("v")
            } else if (branch == "master" || branch == "main") {
                // Master/main branch: use clean version without -dev suffix
                // This ensures production deployments from master have proper version names
                return tag.removePrefix("v")
            } else {
                // Development build with commit hash (for feature branches)
                val shortHashProvider = gitCommand(project, listOf("git", "rev-parse", "--short", "HEAD"))
                val shortHash = shortHashProvider.orNull?.trim() ?: "unknown"
                val commitsSinceTagProvider = gitCommand(project, listOf("git", "rev-list", "$tag..HEAD", "--count"))
                val commitsSinceTag = commitsSinceTagProvider.orNull?.trim() ?: "0"
                return "${tag.removePrefix("v")}-dev.$commitsSinceTag+$shortHash"
            }
        }
        
        // No tags found
        if (branch == "master" || branch == "main") {
            // Master/main branch: use simple version without -dev suffix
            return "1.0.0"
        } else {
            // Development branch: use version with -dev suffix and commit hash
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
