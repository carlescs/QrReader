package cat.company.qrreader.domain.usecase.tags

import android.util.Log
import cat.company.qrreader.domain.model.SuggestedTagModel
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.GenerateContentRequest
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.TextPart
import kotlinx.coroutines.Dispatchers
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

            // Build the prompt
            val existingTagsText = if (existingTags.isNotEmpty()) {
                "Prioritize these existing tags if relevant: ${existingTags.joinToString(", ")}"
            } else {
                ""
            }

            val promptText = """
                Suggest up to 3 short, relevant tags (1-2 words each) for categorizing this barcode content: "$barcodeContent"
                $existingTagsText
                
                Return ONLY the tag names separated by commas, nothing else. Example: Shopping, Food, Receipt
            """.trimIndent()

            // Generate suggestions using the Prompt API
            val request = GenerateContentRequest.Builder(TextPart(promptText))
                .build()
            
            // generateContent is a suspend function that returns a response
            val response = model?.generateContent(request)
            val text = response?.candidates?.firstOrNull()?.text?.trim() ?: ""

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
