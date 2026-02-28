package cat.company.qrreader.domain.usecase.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val GITHUB_RELEASES_URL =
    "https://api.github.com/repos/carlescs/QrReader/releases/latest"
private const val CONNECT_TIMEOUT_MS = 5_000
private const val READ_TIMEOUT_MS = 5_000

/**
 * Represents the outcome of an update check.
 */
sealed class UpdateCheckResult {
    /** A newer version is available on GitHub. */
    data class UpdateAvailable(val latestVersion: String, val releaseUrl: String) : UpdateCheckResult()

    /** The installed version is already the latest. */
    object UpToDate : UpdateCheckResult()

    /** The check could not be completed (e.g. no network, no releases published). */
    data class Error(val message: String) : UpdateCheckResult()
}

/**
 * Use case that checks GitHub releases for a newer app version.
 *
 * Compares the tag name of the latest GitHub release with the installed
 * [currentVersionName].  The comparison is performed on the numeric segments
 * so that tag prefixes like "v" are handled transparently.
 */
class CheckAppUpdateUseCase(private val currentVersionName: String) {

    suspend operator fun invoke(): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val json = fetchLatestReleaseJson()
            val tagName = json.optString("tag_name", "").ifEmpty {
                return@withContext UpdateCheckResult.Error("No releases found")
            }
            val htmlUrl = json.optString("html_url", "")
            val latestVersion = tagName.removePrefix("v")

            if (isNewerVersion(latestVersion, currentVersionName)) {
                UpdateCheckResult.UpdateAvailable(latestVersion, htmlUrl)
            } else {
                UpdateCheckResult.UpToDate
            }
        } catch (e: Exception) {
            UpdateCheckResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun fetchLatestReleaseJson(): JSONObject {
        val url = URL(GITHUB_RELEASES_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
        }
        return try {
            val response = connection.inputStream.bufferedReader().readText()
            JSONObject(response)
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Returns `true` when [latest] is strictly newer than [current].
     *
     * Both strings are split on `.` and each segment is compared numerically.
     * Non-numeric segments fall back to a lexicographic comparison.
     * A missing segment is treated as `0` (e.g. "2.1" == "2.1.0").
     */
    internal fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".")
        val currentParts = current.split(".")
        val size = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until size) {
            val cmp = compareSegment(
                latestParts.getOrElse(i) { "0" },
                currentParts.getOrElse(i) { "0" }
            )
            if (cmp != 0) return cmp > 0
        }
        return false
    }

    /**
     * Compares two version segments.
     * Returns a positive number if [a] > [b], negative if [a] < [b], or `0` if equal.
     * Numeric segments are compared numerically; non-numeric segments lexicographically.
     */
    private fun compareSegment(a: String, b: String): Int {
        val aInt = a.toIntOrNull()
        val bInt = b.toIntOrNull()
        return when {
            aInt != null && bInt != null -> aInt.compareTo(bInt)
            else -> a.compareTo(b)
        }
    }
}
