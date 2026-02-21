package cat.company.qrreader.domain.model

/**
 * Domain model for AI-generated barcode data
 */
data class BarcodeAiData(
    val tags: List<SuggestedTagModel>,
    val description: String
)
