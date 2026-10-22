package com.limaenaccion.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageCompressor {

    /**
     * RNF-10: Las fotos se comprimen antes de guardarse o enviarse (< 500 KB).
     * Comprime un Bitmap a JPEG ajustando la calidad para garantizar que pese menos de 500 KB.
     */
    fun compressBitmap(bitmap: Bitmap, maxBytes: Int = 480 * 1024): ByteArray {
        var quality = 85
        var stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

        // Si sobrepasa los 500 KB, reducir calidad progresivamente
        while (stream.size() > maxBytes && quality > 20) {
            stream.reset()
            quality -= 15
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        }

        // Si aún sobrepasa, redimensionar a la mitad
        if (stream.size() > maxBytes) {
            val scaled = Bitmap.createScaledBitmap(bitmap, bitmap.width / 2, bitmap.height / 2, true)
            stream.reset()
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        }

        return stream.toByteArray()
    }

    /**
     * Convierte el Bitmap comprimido a una cadena Base64 lista para viajar en JSON por Retrofit.
     */
    fun toBase64String(bitmap: Bitmap): String {
        val bytes = compressBitmap(bitmap)
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return "data:image/jpeg;base64,$base64"
    }

    /**
     * Calcula el tamaño en Megabytes con 2 decimales para la UI.
     */
    fun calculateSizeMb(bytes: ByteArray): Double {
        return Math.round((bytes.size.toDouble() / (1024 * 1024)) * 100.0) / 100.0
    }
}
