package cat.company.qrreader.domain.usecase.codecreator

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore

/**
 * Use case to save bitmap to media store and return URI
 */
class SaveBitmapToMediaStoreUseCase {

    operator fun invoke(context: Context, bitmap: Bitmap, title: String = "QrCode"): Uri? {
        return try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, title)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ) ?: return null

            val outputStream = context.contentResolver.openOutputStream(uri) ?: return null
            val bitmapWithWhiteBackground = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            Canvas(bitmapWithWhiteBackground).apply {
                drawColor(Color.WHITE)
                drawBitmap(bitmap, 0f, 0f, null)
            }
            bitmapWithWhiteBackground.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            bitmapWithWhiteBackground.recycle()
            outputStream.close()

            uri
        } catch (_: Exception) {
            null
        }
    }
}

