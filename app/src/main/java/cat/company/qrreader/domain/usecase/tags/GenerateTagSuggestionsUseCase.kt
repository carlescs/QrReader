package cat.company.qrreader.domain.usecase.tags

import android.util.Log
import cat.company.qrreader.domain.model.SuggestedTagModel
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

/**
 * Use case to generate tag suggestions for a barcode using ML Kit GenAI
 */
open class GenerateTagSuggestionsUseCase {
    companion object {
        private const val TAG = "GenerateTagSuggestions"
    }
    
    private var model: GenerativeModel? = null
    
    open suspend operator fun invoke(
        barcodeContent: String,
        barcodeType: String? = null,
        barcodeFormat: String? = null,
        existingTags: List<String>
    ): Result<List<SuggestedTagModel>> = withContext(Dispatchers.IO) {
        try {
            // Initialize model if not already done
            if (model == null) {
                model = Generation.getClient()
            }

            // Check model availability and download if needed
            val status = model?.checkStatus()
            when (status) {
                FeatureStatus.UNAVAILABLE -> {
                    Log.w(TAG, "Gemini Nano unavailable on this device")
                    return@withContext Result.failure(
                        IllegalStateException("Gemini Nano is not supported on this device")
                    )
                }
                FeatureStatus.DOWNLOADABLE -> {
                    Log.i(TAG, "Gemini Nano downloadable - starting download")
                    return@withContext Result.failure(
                        IllegalStateException("Gemini Nano model is being downloaded. Please try again in a moment.")
                    )
                }
                FeatureStatus.DOWNLOADING -> {
                    Log.i(TAG, "Gemini Nano download in progress")
                    return@withContext Result.failure(
                        IllegalStateException("Gemini Nano model download in progress. Please try again in a moment.")
                    )
                }
                FeatureStatus.AVAILABLE -> {
                    Log.d(TAG, "Gemini Nano is available")
                }
                else -> {
                    Log.w(TAG, "Unknown Gemini Nano status: $status")
                    return@withContext Result.failure(
                        IllegalStateException("Unable to determine Gemini Nano availability (status: $status)")
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

            // Build the prompt
            val existingTagsText = if (existingTags.isNotEmpty()) {
                "Prioritize these existing tags if relevant: ${existingTags.joinToString(", ")}"
            } else {
                ""
            }

            val promptText = """
                Suggest up to 3 short, relevant tags (1-2 words each) for categorizing this barcode.
                
                Barcode content: "$barcodeContent"
                $barcodeContext
                $existingTagsText
                
                Return ONLY the tag names separated by commas, nothing else. Example: Shopping, Food, Receipt
            """.trimIndent()
            
            Log.d(TAG, "Generating tags for: $barcodeContent ($barcodeDefinition)")

            // Generate suggestions using the Prompt API
            val request = generateContentRequest(
                TextPart(promptText)
            ) {
                temperature = 0.3f
                topK = 10
                candidateCount = 1
                maxOutputTokens = 50
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

            // Parse the response into tag suggestions
            val suggestions = text
                .split(",")
                .map { it.trim() }
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
     * Attempt to download the Gemini Nano model if it's downloadable
     * @return Flow of download status updates
     */
    open suspend fun downloadModelIfNeeded() = withContext(Dispatchers.IO) {
        try {
            if (model == null) {
                model = Generation.getClient()
            }
            
            val status = model?.checkStatus()
            when (status) {
                FeatureStatus.DOWNLOADABLE -> {
                    Log.i(TAG, "Starting Gemini Nano model download")
                    model?.download()
                        ?.catch { e ->
                            Log.e(TAG, "Error during model download", e)
                        }
                        ?.collect { downloadStatus ->
                            when (downloadStatus) {
                                is DownloadStatus.DownloadStarted -> {
                                    Log.d(TAG, "Gemini Nano download started")
                                }
                                is DownloadStatus.DownloadProgress -> {
                                    Log.d(TAG, "Gemini Nano: ${downloadStatus.totalBytesDownloaded} bytes downloaded")
                                }
                                DownloadStatus.DownloadCompleted -> {
                                    Log.i(TAG, "Gemini Nano download completed")
                                }
                                is DownloadStatus.DownloadFailed -> {
                                    Log.e(TAG, "Gemini Nano download failed: ${downloadStatus.e.message}")
                                }
                            }
                        }
                }
                FeatureStatus.DOWNLOADING -> {
                    Log.i(TAG, "Gemini Nano download already in progress")
                }
                FeatureStatus.AVAILABLE -> {
                    Log.d(TAG, "Gemini Nano already available, no download needed")
                }
                FeatureStatus.UNAVAILABLE -> {
                    Log.w(TAG, "Gemini Nano not available on this device")
                }
                else -> {
                    Log.w(TAG, "Unknown status: $status")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking/downloading model", e)
        }
    }

    open fun cleanup() {
        // Model cleanup is handled automatically by ML Kit
        model = null
    }
}
