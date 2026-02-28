package cat.company.qrreader.domain.usecase.barcode

import android.util.Log
import cat.company.qrreader.domain.model.BarcodeAiData
import cat.company.qrreader.domain.model.SuggestedTagModel
import cat.company.qrreader.domain.usecase.enrichedBarcodeContext
import cat.company.qrreader.domain.usecase.extractJsonObject
import cat.company.qrreader.domain.usecase.languageNameForPrompt
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

/**
 * Use case to generate both tag suggestions and a description for a barcode in a single
 * ML Kit GenAI request, reducing latency and resource usage.
 */
open class GenerateBarcodeAiDataUseCase {

    private var model: GenerativeModel? = null

    /**
     * Generate tags and a description for a barcode in one request.
     *
     * @param barcodeContent The actual barcode content (URL, text, etc.)
     * @param barcodeType Human-readable type (URL, Email, Product, etc.)
     * @param barcodeFormat Human-readable format (QR Code, EAN-13, etc.)
     * @param existingTags List of tag names already in the user's library
     * @param language ISO 639-1 language code for the generated text (e.g., "en", "es")
     * @param humorous Whether the generated tags and description should have a humorous, playful tone instead of a neutral one
     * @param userTitle Optional user-defined label for the barcode that helps contextualize the AI analysis
     * @param userDescription Optional user notes providing additional context about the barcode's purpose or usage
     * @return Result containing [BarcodeAiData] with both tags and description, or an error
     */
    open suspend operator fun invoke(
        barcodeContent: String,
        barcodeType: String? = null,
        barcodeFormat: String? = null,
        existingTags: List<String>,
        language: String = "en",
        humorous: Boolean = false,
        userTitle: String? = null,
        userDescription: String? = null
    ): Result<BarcodeAiData> = withContext(Dispatchers.IO) {
        try {
            if (model == null) {
                model = Generation.getClient()
            }

            val status = model?.checkStatus()
            Log.d(TAG, "Gemini Nano status: $status")
            when (status) {
                FeatureStatus.UNAVAILABLE -> {
                    Log.w(TAG, "Gemini Nano unavailable on this device")
                    return@withContext Result.failure(
                        UnsupportedOperationException(
                            "AI features are not available on this device. " +
                            "This feature requires Gemini Nano, which is only supported on " +
                            "certain devices (Pixel 9+, Galaxy Z Fold7+, etc.)"
                        )
                    )
                }
                FeatureStatus.DOWNLOADABLE -> {
                    Log.i(TAG, "Gemini Nano is downloadable but not yet downloaded")
                    return@withContext Result.failure(
                        IllegalStateException(
                            "AI model is downloading in background. " +
                            "AI features will be available shortly (1-2 minutes). " +
                            "Please try scanning again."
                        )
                    )
                }
                FeatureStatus.DOWNLOADING -> {
                    Log.i(TAG, "Gemini Nano download in progress")
                    return@withContext Result.failure(
                        IllegalStateException(
                            "AI model download in progress. " +
                            "This may take 1-2 minutes depending on your connection. " +
                            "Please try scanning again shortly."
                        )
                    )
                }
                FeatureStatus.AVAILABLE -> {
                    Log.d(TAG, "Gemini Nano is available and ready")
                }
                null -> {
                    Log.e(TAG, "Failed to check Gemini Nano status - model is null")
                    return@withContext Result.failure(
                        IllegalStateException(
                            "Unable to initialize AI model. " +
                            "AI features are temporarily unavailable."
                        )
                    )
                }
                else -> {
                    Log.w(TAG, "Unknown Gemini Nano status: $status")
                    return@withContext Result.failure(
                        IllegalStateException(
                            "AI model status unknown. AI features may not be available."
                        )
                    )
                }
            }

            val barcodeDefinition = buildString {
                if (!barcodeType.isNullOrBlank()) append("Type: $barcodeType")
                if (!barcodeFormat.isNullOrBlank()) {
                    if (isNotEmpty()) append(", ")
                    append("Format: $barcodeFormat")
                }
            }

            val barcodeContext = if (barcodeDefinition.isNotEmpty()) {
                "Barcode definition: $barcodeDefinition"
            } else {
                ""
            }

            val extractedContext = enrichedBarcodeContext(barcodeContent, barcodeType)
            val extractedContextSection = if (extractedContext.isNotEmpty()) {
                "Extracted context:\n$extractedContext"
            } else {
                ""
            }

            val userProvidedContextSection = buildUserProvidedContextSection(userTitle, userDescription)

            val existingTagsText = if (existingTags.isNotEmpty()) {
                "Existing tags you can reuse: ${existingTags.joinToString(", ")}"
            } else {
                ""
            }

            val descriptionRules = buildString {
                appendLine("- For URLs: name the website or service and what it offers")
                appendLine("- For products: mention the product type or brand if recognizable")
                append("- For contacts, Wi-Fi, events, or other types: describe what the barcode provides access to")
                if (humorous) {
                    appendLine()
                    append("- Use a funny, witty, and light-hearted tone — make the user smile!")
                }
            }

            val promptText = """
                Analyze this scanned barcode. Provide up to 3 short tags and a brief description.
                Respond in ${languageNameForPrompt(language)}.
                
                Barcode content: "$barcodeContent"
                $barcodeContext
                $extractedContextSection
                $userProvidedContextSection
                $existingTagsText
                
                Tags rules:
                - Prefer reusing existing tags when they fit
                - Choose specific, meaningful categories (e.g., Work, Travel, Health, Finance, Shopping)
                - Capitalize each tag (e.g., "Loyalty Card", "Online Order")
                - Avoid generic tags like "Barcode", "Item", or "Other"
                
                Description rules:
                - 1-2 sentences, under $MAX_DESCRIPTION_LENGTH characters
                $descriptionRules
                
                Respond ONLY with valid JSON in this exact format, nothing else:
                {"tags": ["Tag1", "Tag2", "Tag3"], "description": "Your description here."}
            """.trimIndent()

            Log.d(TAG, "Generating AI data for: $barcodeContent ($barcodeDefinition)")

            val request = generateContentRequest(TextPart(promptText)) {
                temperature = 0.4f
                topK = 20
                candidateCount = 1
                maxOutputTokens = 150
            }

            val response = model?.generateContent(request)
            val text = response?.candidates?.firstOrNull()?.text?.trim() ?: ""

            Log.d(TAG, "Generated AI data: $text")

            if (text.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("Empty response from model")
                )
            }

            val jsonText = extractJsonObject(text) ?: throw JSONException("No JSON object found")
            val json = JSONObject(jsonText)

            val tagNames: List<String> = try {
                val array = json.getJSONArray("tags")
                (0 until array.length()).map { array.getString(it).trim() }
            } catch (e: JSONException) {
                Log.w(TAG, "JSON tags parsing failed, falling back to comma-split: ${e.message}")
                text.split(",").map { it.trim() }
            }

            val tags = tagNames
                .filter { it.isNotEmpty() && it.length <= 30 }
                .distinct()
                .take(3)
                .map { SuggestedTagModel(name = it, isSelected = false) }

            val rawDescription = try {
                json.getString("description").trim()
            } catch (e: JSONException) {
                Log.w(TAG, "JSON description parsing failed: ${e.message}")
                ""
            }

            val description = when {
                rawDescription.length > MAX_DESCRIPTION_LENGTH ->
                    rawDescription.take(MAX_DESCRIPTION_LENGTH - 3) + "..."
                else -> rawDescription
            }

            Result.success(BarcodeAiData(tags = tags, description = description))
        } catch (e: Exception) {
            Log.e(TAG, "Error generating AI data for barcode", e)
            Result.failure(e)
        }
    }

    /**
     * Attempt to download the Gemini Nano model if it's downloadable.
     * Should be called once during app initialization.
     */
    open suspend fun downloadModelIfNeeded() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Checking Gemini Nano model availability...")

            if (model == null) {
                model = Generation.getClient()
            }

            val generativeModel = model ?: run {
                Log.e(TAG, "✗ Failed to get GenerativeModel client")
                return@withContext
            }

            val status = generativeModel.checkStatus()
            Log.i(TAG, "Gemini Nano status: $status")

            when (status) {
                FeatureStatus.DOWNLOADABLE -> {
                    Log.i(TAG, "Gemini Nano is available but not downloaded. Starting download (~150-200MB)...")
                    try {
                        generativeModel.download()
                            .catch { e ->
                                Log.e(TAG, "Error during model download stream", e)
                                throw e
                            }
                            .collect { downloadStatus ->
                                when (downloadStatus) {
                                    is DownloadStatus.DownloadStarted -> {
                                        Log.i(TAG, "✓ Gemini Nano download started.")
                                    }
                                    is DownloadStatus.DownloadProgress -> {
                                        val mb = downloadStatus.totalBytesDownloaded / 1_000_000
                                        Log.d(TAG, "  Download progress: ${mb}MB downloaded...")
                                    }
                                    DownloadStatus.DownloadCompleted -> {
                                        Log.i(TAG, "✓ Gemini Nano download completed successfully!")
                                    }
                                    is DownloadStatus.DownloadFailed -> {
                                        Log.e(TAG, "✗ Gemini Nano download failed: ${downloadStatus.e.message}", downloadStatus.e)
                                    }
                                }
                            }
                    } catch (e: Exception) {
                        Log.e(TAG, "✗ Failed to start Gemini Nano download", e)
                    }
                }
                FeatureStatus.DOWNLOADING -> {
                    Log.i(TAG, "Gemini Nano download already in progress...")
                    try {
                        generativeModel.download()
                            .catch { e -> Log.e(TAG, "Error monitoring download progress", e) }
                            .collect { downloadStatus ->
                                when (downloadStatus) {
                                    is DownloadStatus.DownloadProgress -> {
                                        val mb = downloadStatus.totalBytesDownloaded / 1_000_000
                                        Log.d(TAG, "  Download progress: ${mb}MB downloaded...")
                                    }
                                    DownloadStatus.DownloadCompleted -> {
                                        Log.i(TAG, "✓ Gemini Nano download completed successfully!")
                                    }
                                    is DownloadStatus.DownloadFailed -> {
                                        Log.e(TAG, "✗ Gemini Nano download failed: ${downloadStatus.e.message}", downloadStatus.e)
                                    }
                                    else -> { /* Ignore other statuses */ }
                                }
                            }
                    } catch (e: Exception) {
                        Log.e(TAG, "✗ Error monitoring download", e)
                    }
                }
                FeatureStatus.AVAILABLE -> {
                    Log.i(TAG, "✓ Gemini Nano is already available. No download needed.")
                }
                FeatureStatus.UNAVAILABLE -> {
                    Log.w(TAG, "✗ Gemini Nano is NOT available on this device.")
                }
                else -> {
                    Log.w(TAG, "⚠ Unknown Gemini Nano status: $status")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error checking/downloading Gemini Nano model", e)
        }
    }

    /**
     * Check whether the current device supports AI features (Gemini Nano / ML Kit Prompt API).
     *
     * @return `true` if the device can run AI features (model is available, downloadable, or
     *         currently downloading), `false` if it is permanently unavailable on this device.
     */
    open suspend fun isAiSupportedOnDevice(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (model == null) {
                model = Generation.getClient()
            }
            model?.checkStatus() != FeatureStatus.UNAVAILABLE
        } catch (e: Exception) {
            Log.e(TAG, "Error checking AI device support", e)
            false
        }
    }

    open fun cleanup() {
        model = null
    }

    companion object {
        private const val TAG = "GenerateBarcodeAiData"
        private const val MAX_DESCRIPTION_LENGTH = 200
        internal const val MAX_USER_TITLE_LENGTH = 100
        internal const val MAX_USER_DESCRIPTION_LENGTH = 200

        /**
         * Builds the "User-provided context" section to inject into the AI prompt.
         *
         * Each field is normalised (whitespace collapsed) and truncated to avoid bloating
         * the prompt or exceeding the model's context window.  Returns an empty string when
         * both [userTitle] and [userDescription] are blank, so callers can omit the section
         * from the prompt entirely.
         */
        internal fun buildUserProvidedContextSection(
            userTitle: String?,
            userDescription: String?
        ): String {
            val normalizedTitle = userTitle?.trim()
                ?.replace(Regex("\\s+"), " ")
                ?.take(MAX_USER_TITLE_LENGTH)
            val normalizedDescription = userDescription?.trim()
                ?.replace(Regex("\\s+"), " ")
                ?.take(MAX_USER_DESCRIPTION_LENGTH)

            val context = buildString {
                if (!normalizedTitle.isNullOrBlank()) appendLine("User-provided title: $normalizedTitle")
                if (!normalizedDescription.isNullOrBlank()) appendLine("User-provided description: $normalizedDescription")
            }.trim()

            return if (context.isNotEmpty()) {
                "User-provided context (use this to refine the tags and description):\n$context"
            } else {
                ""
            }
        }
    }
}
