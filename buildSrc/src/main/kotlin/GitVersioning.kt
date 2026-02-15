import org.gradle.api.Project
import java.io.ByteArrayOutputStream

object GitVersioning {
    fun getVersionCode(project: Project): Int {
        val count = runCommand(project, "git", "rev-list", "--count", "HEAD").toIntOrNull()
        if (count == null) {
            project.logger.warn("Failed to get Git commit count, falling back to version code 1")
            return 1
        }
        return count
    }

    fun getVersionName(project: Project): String {
        // Try to get the latest tag
        val tag = runCommand(project, "git", "describe", "--tags", "--abbrev=0").trim()
        
        if (tag.isNotEmpty()) {
            // Check if current commit is tagged
            val currentCommit = runCommand(project, "git", "rev-parse", "HEAD").trim()
            val tagCommit = runCommand(project, "git", "rev-list", "-n", "1", tag).trim()
            
            if (currentCommit == tagCommit) {
                // Clean release build
                return tag.removePrefix("v")
            } else {
                // Development build with commit hash
                val shortHash = runCommand(project, "git", "rev-parse", "--short", "HEAD").trim()
                val commitsSinceTag = runCommand(project, "git", "rev-list", "$tag..HEAD", "--count").trim()
                return "${tag.removePrefix("v")}-dev.$commitsSinceTag+$shortHash"
            }
        }
        
        // No tags found, use default
        val shortHash = runCommand(project, "git", "rev-parse", "--short", "HEAD").trim()
        return "0.0.1-dev+$shortHash"
    }

    private fun runCommand(project: Project, vararg command: String): String {
        return try {
            val stdout = ByteArrayOutputStream()
            val result = project.exec {
                commandLine(*command)
                standardOutput = stdout
                isIgnoreExitValue = true
            }
            if (result.exitValue == 0) {
                stdout.toString().trim()
            } else {
                ""
            }
        } catch (e: Exception) {
            project.logger.warn("Failed to run command: ${command.joinToString(" ")} - ${e.message}")
            ""
        }
    }
}
