package cat.company.qrreader.domain.model

import kotlin.random.Random

/**
 * Domain model for a suggested tag
 */
data class SuggestedTagModel(
    val name: String,
    val isSelected: Boolean = false,
    val color: String = generateRandomColor()
) {
    companion object {
        /**
         * Generate a random pastel color as a hex string.
         * Pastel colors are more visually appealing for tags.
         */
        fun generateRandomColor(): String {
            // Generate pastel colors by using high base values (127-255 range)
            val red = Random.nextInt(128, 256)
            val green = Random.nextInt(128, 256)
            val blue = Random.nextInt(128, 256)
            return String.format("#%02X%02X%02X", red, green, blue)
        }
    }
}
