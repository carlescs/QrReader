import org.gradle.api.Project
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
        var command: List<String>
        val rootDir: Property<File>
    }

    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): String {
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        return try {
            val execResult = execOperations.exec {
                commandLine(parameters.command)
                workingDir = parameters.rootDir.get()
                standardOutput = output
                errorOutput = error
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
        return count
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
            parameters.command = command
            parameters.rootDir.set(project.rootDir)
        }
    }
}
