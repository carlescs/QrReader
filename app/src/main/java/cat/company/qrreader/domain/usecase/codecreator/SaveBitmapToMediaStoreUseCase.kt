package cat.company.qrreader.domain.usecase.codecreator

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()

            uri
        } catch (_: Exception) {
            null
        }
    }
}

