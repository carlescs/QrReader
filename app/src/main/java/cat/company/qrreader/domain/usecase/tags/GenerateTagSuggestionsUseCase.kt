package cat.company.qrreader.domain.usecase.tags

import android.content.Context
import android.os.Build
import cat.company.qrreader.domain.model.SuggestedTagModel
import com.google.mlkit.genai.GenerativeModel
import com.google.mlkit.genai.GenerativeModelConfig
import com.google.mlkit.genai.prompt.GenerationConfig
import com.google.mlkit.genai.prompt.ModelAvailabilityStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case to generate tag suggestions for a barcode using ML Kit GenAI
 */
class GenerateTagSuggestionsUseCase(
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
                val config = GenerativeModelConfig(
                    GenerationConfig(temperature = 0.3f, maxOutputTokens = 100)
                )
                model = GenerativeModel(context, config)
            }

            // Check model availability
            val status = model?.checkAvailability()
            if (status != ModelAvailabilityStatus.AVAILABLE) {
                return@withContext Result.failure(
                    IllegalStateException("Gemini Nano is not available on this device")
                )
            }

            // Build the prompt
            val existingTagsText = if (existingTags.isNotEmpty()) {
                "Prioritize these existing tags if relevant: ${existingTags.joinToString(", ")}"
            } else {
                ""
            }

            val prompt = """
                Suggest up to 3 short, relevant tags (1-2 words each) for categorizing this barcode content: "$barcodeContent"
                $existingTagsText
                
                Return ONLY the tag names separated by commas, nothing else. Example: Shopping, Food, Receipt
            """.trimIndent()

            // Generate suggestions
            val response = model?.generateContent(prompt)
            val text = response?.text?.trim() ?: ""

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

    fun cleanup() {
        model?.close()
        model = null
    }
}
