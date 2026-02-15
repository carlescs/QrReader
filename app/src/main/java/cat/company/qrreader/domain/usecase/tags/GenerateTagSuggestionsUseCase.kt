package cat.company.qrreader.domain.usecase.tags

import android.util.Log
import cat.company.qrreader.domain.model.SuggestedTagModel
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.GenerateContentRequest
import com.google.mlkit.genai.prompt.GenerateContentResult
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.TextPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

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

            // Check model availability
            val status = model?.checkStatus()
            if (status != FeatureStatus.AVAILABLE) {
                Log.w(TAG, "Gemini Nano not available. Status: $status")
                return@withContext Result.failure(
                    IllegalStateException("Gemini Nano is not available on this device (status: $status)")
                )
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

            // Generate suggestions using the Prompt API (callback-based)
            val request = GenerateContentRequest.Builder(TextPart(promptText))
                .build()
            
            // Convert callback-based API to suspend function
            val text = suspendCancellableCoroutine<String> { continuation ->
                model?.generateContent(request) { result ->
                    when (result) {
                        is GenerateContentResult.Success -> {
                            val outputText = result.outputText?.trim() ?: ""
                            Log.d(TAG, "Generated content: $outputText")
                            continuation.resume(outputText)
                        }
                        is GenerateContentResult.Failure -> {
                            Log.w(TAG, "Content generation failed: ${result.message}")
                            continuation.resume("")
                        }
                    }
                }
            }

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

    open fun cleanup() {
        // Model cleanup is handled automatically by ML Kit
        model = null
    }
}
