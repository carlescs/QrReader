package cat.company.qrreader.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

/**
 * Utils class
 */
class Utils {
    companion object {

        /**
         * Parse color from string
         */
        fun parseColor(color: String): Color? {
            return try {
                Color(android.graphics.Color.parseColor(color))
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Color to string
         */
        fun colorToString(color: Color): String {
            return "#${Integer.toHexString(color.toArgb())}"
        }

        /**
         * Get color based on background luminance
         */
        fun colorBasedOnBackground(color: Color?): Color {
            return if (color == null || color.luminance() > 0.5f) Color.Black
            else Color.White
        }
    }
}