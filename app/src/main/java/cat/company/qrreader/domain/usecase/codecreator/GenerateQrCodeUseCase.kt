package cat.company.qrreader.domain.usecase.codecreator

import android.graphics.Bitmap

/**
 * Use case to generate QR code bitmap from text
 */
open class GenerateQrCodeUseCase {

    open operator fun invoke(text: String): Bitmap? {
        if (text.isEmpty()) return null

        return try {
            val bos = java.io.ByteArrayOutputStream()
            qrcode.QRCode(text).render().writeImage(bos)
            android.graphics.BitmapFactory.decodeByteArray(
                bos.toByteArray(),
                0,
                bos.toByteArray().size
            )
        } catch (_: Exception) {
            null
        }
    }
}

