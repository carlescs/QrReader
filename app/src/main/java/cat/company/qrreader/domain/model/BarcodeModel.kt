package cat.company.qrreader.domain.model

import java.util.Date

/**
 * Domain model for a saved barcode
 */
data class BarcodeModel(
    val id: Int = 0,
    val date: Date = Date(),
    val type: Int,
    val format: Int,
    val title: String? = null,
    val description: String? = null,
    val barcode: String,
    val aiGeneratedDescription: String? = null,
    val isFavorite: Boolean = false,
    val isLocked: Boolean = false,
)

