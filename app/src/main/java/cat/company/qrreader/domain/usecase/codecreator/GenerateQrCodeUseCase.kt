package cat.company.qrreader.domain.usecase.codecreator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color

/**
 * Use case to generate QR code bitmap from text
 */
open class GenerateQrCodeUseCase {

    open operator fun invoke(text: String): Bitmap? {
        if (text.isEmpty()) return null

        return try {
            val bos = java.io.ByteArrayOutputStream()
            qrcode.QRCode(text).render().writeImage(bos)
            val bytes = bos.toByteArray()
            val qrBitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            addWhiteMargin(qrBitmap)
        } catch (_: Exception) {
            null
        }
    }

    private fun addWhiteMargin(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(
            bitmap.width + MARGIN_PX * 2,
            bitmap.height + MARGIN_PX * 2,
            Bitmap.Config.ARGB_8888
        )
        result.eraseColor(Color.WHITE)
        Canvas(result).drawBitmap(bitmap, MARGIN_PX.toFloat(), MARGIN_PX.toFloat(), null)
        return result
    }

    companion object {
        const val MARGIN_PX = 10
    }
}

