package com.handysparksoft.trackmap.core.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

object Base64Utils {

    fun encodeImage(bitmap: Bitmap, quality: Int): String? {
        val baos = ByteArrayOutputStream()
        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            quality,
            baos
        )
        val byteArrayResult = baos.toByteArray()
        return Base64.encodeToString(byteArrayResult, Base64.DEFAULT)
    }

    fun getBase64Bitmap(encodedImage: String): Bitmap {
        val decodedString: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }
}
