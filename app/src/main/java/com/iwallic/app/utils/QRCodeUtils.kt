package com.iwallic.app.utils

import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.*
import android.graphics.Canvas
import com.iwallic.app.R
import android.content.Context
import android.support.v4.content.ContextCompat
import android.graphics.drawable.BitmapDrawable

object QRCodeUtils {
    fun generate(content: String,context:Context, size: Int = 300): Bitmap? {
        val result: BitMatrix
        try {
            result = MultiFormatWriter().encode(content,
                    BarcodeFormat.QR_CODE, size, size, Hashtable<EncodeHintType, Any>().apply {
                put(EncodeHintType.CHARACTER_SET, "utf-8")
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.MARGIN, -1)
            })
        } catch (iae: IllegalArgumentException) {
            // Unsupported format
            return null
        }

        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (result.get(x, y)) BLACK else WHITE
            }
        }
        var bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, size, 0, 0, w, h)


        // 图片绘制在二维码中央，合成二维码图片
        var logoBmp = (ContextCompat.getDrawable(context, R.drawable.logo) as BitmapDrawable).bitmap
        val targetSize = 80f
        val scaleFactor = targetSize / logoBmp.width
        val logoWidth = logoBmp.width
        val logoHeight = logoBmp.height
        try {
            val canvas = Canvas(bitmap)
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, null)
            canvas.scale(scaleFactor, scaleFactor, w / 2.0f,
                    h / 2.0f)
            canvas.drawBitmap(logoBmp, (w - logoWidth) / 2.0f,
                    (h - logoHeight) / 2.0f, null)
            canvas.save(Canvas.ALL_SAVE_FLAG)
            canvas.restore()
        } catch (e: Exception) {
            bitmap = null
            e.stackTrace
        }

        return bitmap
    }
}
