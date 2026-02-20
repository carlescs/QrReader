package cat.company.qrreader.domain.usecase.tags

import android.util.Log
import cat.company.qrreader.domain.model.SuggestedTagModel
import cat.company.qrreader.domain.usecase.enrichedBarcodeContext
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
 * Use case to generate tag suggestions for a barcode using ML Kit GenAI
 */
open class GenerateTagSuggestionsUseCase {
    companion object {
        private const val TAG = "GenerateTagSuggestions"

        /**
         * Extract JSON object from AI response text that may contain extra text.
         * Handles cases like:
         * - "Here's the JSON: {\"tags\": [...]}"
         * - "{\"tags\": [...]} Hope this helps!"
         * - Just the JSON: "{\"tags\": [...]}"
         */
        private fun extractJsonObject(text: String): String {
            // Find first { and last } to extract JSON object
            val startIndex = text.indexOf('{')
            val endIndex = text.lastIndexOf('}')

            if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
                // No valid JSON braces found, return original text
                return text
            }

            return text.substring(startIndex, endIndex + 1)
        }
    }
    
    private var model: GenerativeModel? = null
    
    open suspend operator fun invoke(
        barcodeContent: String,
        barcodeType: String? = null,
        barcodeFormat: String? = null,
        existingTags: List<String>,
        language: String = "en"
    ): Result<List<SuggestedTagModel>> = withContext(Dispatchers.IO) {
        try {
            // Initialize model if not already done
            if (model == null) {
                model = Generation.getClient()
            }

            // Check model availability and download if needed
            val status = model?.checkStatus()
            Log.d(TAG, "Gemini Nano status: $status")
            when (status) {
                FeatureStatus.UNAVAILABLE -> {
                    Log.w(TAG, "Gemini Nano unavailable on this device")
                    return@withContext Result.failure(
                        UnsupportedOperationException(
                            "AI tag suggestions are not available on this device. " +
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
                            "Tag suggestions will be available shortly (1-2 minutes). " +
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
                            "Tag suggestions are temporarily unavailable."
                        )
                    )
                }
                else -> {
                    Log.w(TAG, "Unknown Gemini Nano status: $status")
                    return@withContext Result.failure(
                        IllegalStateException(
                            "AI model status unknown. Tag suggestions may not be available."
                        )
                    )
                }
            }

            // Build barcode definition context
            val barcodeDefinition = buildString {
                if (!barcodeType.isNullOrBlank()) {
                    append("Type: $barcodeType")
                }
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

            // Extract additional context from the barcode content itself
            val extractedContext = enrichedBarcodeContext(barcodeContent, barcodeType)
            val extractedContextSection = if (extractedContext.isNotEmpty()) {
                "Extracted context:\n$extractedContext"
            } else {
                ""
            }

            // Build the prompt
            val existingTagsText = if (existingTags.isNotEmpty()) {
                "Existing tags you can reuse: ${existingTags.joinToString(", ")}"
            } else {
                ""
            }

            val promptText = """
                Suggest up to 3 short, relevant tags (1-2 words each) to categorize this scanned barcode.
                Respond in ${languageNameForPrompt(language)}.
                
                Barcode content: "$barcodeContent"
                $barcodeContext
                $extractedContextSection
                $existingTagsText
                
                - Prefer reusing existing tags when they fit
                - Choose specific, meaningful categories (e.g., Work, Travel, Health, Finance, Shopping)
                - Capitalize each tag (e.g., "Loyalty Card", "Online Order")
                - Avoid generic tags like "Barcode", "Item", or "Other"
                
                Respond ONLY with valid JSON in this exact format, nothing else:
                {"tags": ["Tag1", "Tag2", "Tag3"]}
            """.trimIndent()
            
            Log.d(TAG, "Generating tags for: $barcodeContent ($barcodeDefinition)")

            // Generate suggestions using the Prompt API
            val request = generateContentRequest(
                TextPart(promptText)
            ) {
                temperature = 0.3f
                topK = 10
                candidateCount = 1
                maxOutputTokens = 60
            }
            
            // Generate content synchronously (suspend function)
            val response = model?.generateContent(request)
            val text = response?.candidates?.firstOrNull()?.text?.trim() ?: ""
            
            Log.d(TAG, "Generated content: $text")

            if (text.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("Empty response from model")
                )
            }

            // Parse JSON response; fall back to comma-split for robustness
            val tagNames: List<String> = try {
                // Extract JSON object from response (may contain extra text)
                val jsonText = extractJsonObject(text)
                val json = JSONObject(jsonText)
                val array = json.getJSONArray("tags")
                (0 until array.length()).map { array.getString(it).trim() }
            } catch (e: JSONException) {
                Log.w(TAG, "JSON parsing failed, falling back to comma-split: ${e.message}")
                Log.w(TAG, "Response text was: $text")
                text.split(",").map { it.trim() }
            }

            // Parse the response into tag suggestions
            val suggestions = tagNames
                .filter { it.isNotEmpty() && it.length <= 30 }
                .distinct()
                .take(3)
                .map { SuggestedTagModel(name = it, isSelected = true) }

            if (suggestions.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("No valid tag suggestions generated")
                )
            }

            Result.success(suggestions)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating tag suggestions", e)
            Result.failure(e)
        }
    }

    /**
     * Attempt to download the Gemini Nano model if it's downloadable.
     * This should be called once during app initialization to ensure the model
     * is available when users start scanning barcodes.
     */
    open suspend fun downloadModelIfNeeded() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Checking Gemini Nano model availability...")
            
            if (model == null) {
                model = Generation.getClient()
            }
            
            val generativeModel = model
            if (generativeModel == null) {
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
                                        Log.i(TAG, "✓ Gemini Nano download started. This may take 1-2 minutes.")
                                    }
                                    is DownloadStatus.DownloadProgress -> {
                                        val mb = downloadStatus.totalBytesDownloaded / 1_000_000
                                        Log.d(TAG, "  Download progress: ${mb}MB downloaded...")
                                    }
                                    DownloadStatus.DownloadCompleted -> {
                                        Log.i(TAG, "✓ Gemini Nano download completed successfully! AI tag suggestions are now available.")
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
                    Log.i(TAG, "Gemini Nano download already in progress. Waiting for completion...")
                    // Collect download progress for ongoing downloads too
                    try {
                        generativeModel.download()
                            .catch { e ->
                                Log.e(TAG, "Error monitoring download progress", e)
                            }
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
                    Log.w(TAG, "✗ Gemini Nano is NOT available on this device. AI tag suggestions will not work.")
                    Log.w(TAG, "  Supported devices: Pixel 9+, Galaxy Z Fold7+, Xiaomi 15, and other devices with AICore service.")
                }
                else -> {
                    Log.w(TAG, "⚠ Unknown Gemini Nano status: $status")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error checking/downloading Gemini Nano model", e)
            Log.e(TAG, "  Tag suggestions will not be available. Error: ${e.message}")
        }
    }

    open fun cleanup() {
        // Model cleanup is handled automatically by ML Kit
        model = null
    }
}
