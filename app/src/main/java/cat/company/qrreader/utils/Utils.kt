package cat.company.qrreader.utils

import androidx.compose.ui.graphics.Color
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

    }
}