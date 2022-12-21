/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cat.company.qrreader.camera

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.camera.view.TransformExperimental
import androidx.camera.view.transform.CoordinateTransform
import com.google.mlkit.vision.barcode.common.Barcode
import kotlin.math.roundToInt

@TransformExperimental
/**
 * A Drawable that handles displaying a QR Code's data and a bounding box around the QR code.
 */
class QrCodeDrawable(private val barcode: Barcode, private val coordinateTransform: CoordinateTransform) : Drawable() {
    private val boundingRectPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.LTGRAY
        strokeWidth = 5F
        alpha = 200
    }

    private val contentRectPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.LTGRAY
        alpha = 255
    }

    private val contentTextPaint = Paint().apply {
        color = Color.BLACK
        alpha = 255
        textSize = 50F
    }

    private val contentPadding = 25
    private var textWidth = contentTextPaint.measureText(barcode.displayValue).toInt()

    override fun draw(canvas: Canvas) {
        val rect = barcode.boundingBox!!
        val rectF=RectF(rect)
        coordinateTransform.mapRect(rectF)
        val boundingBox=Rect().apply {
            this.right= rectF.right.roundToInt()
            this.top= rectF.top.roundToInt()
            this.bottom= rectF.bottom.roundToInt()
            this.left= rectF.left.roundToInt()
        }
        canvas.drawRect(boundingBox, boundingRectPaint)
        canvas.drawRect(
            Rect(
                boundingBox.left,
                boundingBox.bottom + contentPadding/2,
                boundingBox.left + textWidth + contentPadding*2,
                boundingBox.bottom + contentTextPaint.textSize.toInt() + contentPadding),
            contentRectPaint
        )
        canvas.drawText(
            barcode.displayValue?:"No code",
            (boundingBox.left + contentPadding).toFloat(),
            (boundingBox.bottom + contentPadding*2).toFloat(),
            contentTextPaint
        )
    }

    override fun setAlpha(alpha: Int) {
        boundingRectPaint.alpha = alpha
        contentRectPaint.alpha = alpha
        contentTextPaint.alpha = alpha
    }

    override fun setColorFilter(colorFiter: ColorFilter?) {
        boundingRectPaint.colorFilter = colorFilter
        contentRectPaint.colorFilter = colorFilter
        contentTextPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}