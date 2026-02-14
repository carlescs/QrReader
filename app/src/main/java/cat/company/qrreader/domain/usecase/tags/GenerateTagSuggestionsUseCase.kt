package cat.company.qrreader.domain.usecase.tags

import android.content.Context
import android.os.Build
import cat.company.qrreader.domain.model.SuggestedTagModel
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.FeatureStatus
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Use case to generate tag suggestions for a barcode using ML Kit GenAI
 */
open class GenerateTagSuggestionsUseCase(
    private val context: Context
) {
    private var model: GenerativeModel? = null
    
    suspend operator fun invoke(
        barcodeContent: String,
        existingTags: List<String>
    ): Result<List<SuggestedTagModel>> = withContext(Dispatchers.IO) {
        try {
            // Check if device supports Gemini Nano (API 26+)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                return@withContext Result.failure(
                    UnsupportedOperationException("ML Kit GenAI requires Android 8.0 (API 26) or higher")
                )
            }

            // Initialize model if not already done
            if (model == null) {
                model = Generation.getClient()
            }

            // Check model availability
            val status = model?.checkStatus()
            if (status != FeatureStatus.AVAILABLE) {
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
            val request = generateContentRequest(TextPart(promptText)) {
                temperature = 0.3f
                maxOutputTokens = 100
            }
            
            val response = model?.generateContent(request)?.await()
            val text = response?.candidates?.firstOrNull()?.text?.trim() ?: ""

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
            Result.failure(e)
        }
    }

    open fun cleanup() {
        // No explicit close method in the new API
        model = null
    }
}
