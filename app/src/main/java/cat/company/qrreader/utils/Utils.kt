package cat.company.qrreader.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

class Utils {
    companion object {
        fun parseColor(color: String): Color? {
            return try {
                Color(android.graphics.Color.parseColor(color))
            } catch (e: Exception) {
                null
            }
        }

        fun colorToString(color: Color): String {
            return "#${Integer.toHexString(color.toArgb())}"
        }

        fun colorBasedOnBackground(color: Color?): Color {
            return if (color == null || color.luminance() > 0.5f) Color.Black
            else Color.White
        }
    }
}