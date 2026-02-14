package cat.company.qrreader.domain.model

/**
 * Domain model for a suggested tag
 */
data class SuggestedTagModel(
    val name: String,
    val isSelected: Boolean = true
)
