package cat.company.qrreader.domain.usecase.barcode

import android.util.Log
import cat.company.qrreader.domain.usecase.languageNameForPrompt
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

/**
 * Use case to generate a human-readable description for a barcode using ML Kit GenAI
 */
open class GenerateBarcodeDescriptionUseCase {
    companion object {
        private const val TAG = "GenerateBarcodeDesc"
        private const val MAX_DESCRIPTION_LENGTH = 200
    }
    
    private var model: GenerativeModel? = null
    
    /**
     * Generate a description for a barcode
     * 
     * @param barcodeContent The actual barcode content (URL, text, etc.)
     * @param barcodeType Human-readable type (URL, Email, Product, etc.)
     * @param barcodeFormat Human-readable format (QR Code, EAN-13, etc.)
     * @param language ISO 639-1 language code for the generated description (e.g., "en", "es")
     * @return Result containing the generated description or error
     */
    open suspend operator fun invoke(
        barcodeContent: String,
        barcodeType: String? = null,
        barcodeFormat: String? = null,
        language: String = "en"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Initialize model if not already done
            if (model == null) {
                model = Generation.getClient()
            }

            // Check model availability
            val status = model?.checkStatus()
            Log.d(TAG, "Gemini Nano status: $status")
            when (status) {
                FeatureStatus.UNAVAILABLE -> {
                    Log.w(TAG, "Gemini Nano unavailable on this device")
                    return@withContext Result.failure(
                        UnsupportedOperationException(
                            "AI description generation is not available on this device. " +
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
                            "Descriptions will be available shortly (1-2 minutes). " +
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
                            "Description generation is temporarily unavailable."
                        )
                    )
                }
                else -> {
                    Log.w(TAG, "Unknown Gemini Nano status: $status")
                    return@withContext Result.failure(
                        IllegalStateException(
                            "AI model status unknown. Description generation may not be available."
                        )
                    )
                }
            }

            // Build the prompt
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

            val promptText = """
                Generate a brief, helpful description (1-2 sentences, max $MAX_DESCRIPTION_LENGTH characters) for this barcode.
                Explain what it is and what it might be used for.
                Respond in ${languageNameForPrompt(language)}.
                
                Barcode content: "$barcodeContent"
                $barcodeContext
                
                - For URLs: name the website or service and what it offers
                - For products: mention the product type or brand if recognizable
                - For contacts, Wi-Fi, events, or other types: describe what the barcode provides access to
                
                Respond ONLY with valid JSON in this exact format, nothing else:
                {"description": "Your description here."}
            """.trimIndent()
            
            Log.d(TAG, "Generating description for: $barcodeContent ($barcodeDefinition)")

            // Generate description using the Prompt API
            val request = generateContentRequest(
                TextPart(promptText)
            ) {
                temperature = 0.4f  // Slightly lower for more consistent descriptions
                topK = 20
                candidateCount = 1
                maxOutputTokens = 100  // Enough for ~200 characters
            }
            
            // Generate content synchronously (suspend function)
            val response = model?.generateContent(request)
            val text = response?.candidates?.firstOrNull()?.text?.trim() ?: ""
            
            Log.d(TAG, "Generated description: $text")

            if (text.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("Empty response from model")
                )
            }

            // Parse JSON response; fall back to raw text for robustness
            val rawDescription = try {
                val json = JSONObject(text)
                json.getString("description").trim()
            } catch (e: JSONException) {
                Log.w(TAG, "JSON parsing failed, using raw text: ${e.message}")
                text
            }

            // Truncate if necessary and clean up
            val description = if (rawDescription.length > MAX_DESCRIPTION_LENGTH) {
                rawDescription.take(MAX_DESCRIPTION_LENGTH - 3) + "..."
            } else {
                rawDescription
            }

            Result.success(description)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating barcode description", e)
            Result.failure(e)
        }
    }

    open fun cleanup() {
        // Model cleanup is handled automatically by ML Kit
        model = null
    }
}
