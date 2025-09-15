package com.lhr.flighttracker.core.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeGenerator {
    /**
     * 生成 QR Code Bitmap
     * @param data 要編碼的字串
     * @param width 圖片寬度 (px)
     * @param height 圖片高度 (px)
     * @param qrCodeColor QR Code 的顏色 (Android Color Int)
     * @param backgroundColor 背景顏色 (Android Color Int)
     * @return Bitmap?
     */
    fun generateQrCodeBitmap(
        data: String,
        width: Int = 512,
        height: Int = 512,
        qrCodeColor: Int = android.graphics.Color.BLACK,
        backgroundColor: Int = android.graphics.Color.WHITE
    ): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val hints = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height, hints)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) qrCodeColor else backgroundColor)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
