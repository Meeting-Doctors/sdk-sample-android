package com.meetingdoctors.chat.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Point
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


/**
 * Created by Héctor Manrique on 4/8/21.
 */
class BitmapHelper {
    companion object {
        const val JPG_EXTENSION = ".jpg"
        const val TEMPORAL_DIRECTORY_NAME = "md_temp"
        const val TEMPORAL_DIRECTORY = "/md_temp"
        fun getImageSize(imagePath: String?): Point {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, options)
            return Point(options.outWidth, options.outHeight)
        }

        @Throws(Exception::class)
        fun saveBitmapToTempFile(context: Context, bitmap: Bitmap): File {
            val directory = context.cacheDir.toString() + TEMPORAL_DIRECTORY
            val dir = File(directory)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, TEMPORAL_DIRECTORY_NAME + Date().time + JPG_EXTENSION)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            return file
        }

        fun rotateAndResizeImage(imageFile: String, maxImageArea: Long, pictureFromCamera: Boolean): Bitmap {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imageFile, options)
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, maxImageArea)
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            val resizedBitmap = BitmapFactory.decodeFile(imageFile, options)
            // Rotate if necessary
            val rotation = getImageRotation(imageFile, pictureFromCamera)
            if (rotation.toFloat() != 0f) {
                val matrix = Matrix()
                matrix.preRotate(rotation.toFloat())
                return Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.width, resizedBitmap.height, matrix, true)
            }
            return resizedBitmap
        }

        private fun calculateInSampleSize(options: BitmapFactory.Options, maxImageArea: Long): Int {
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            while (height / inSampleSize * (width / inSampleSize) > maxImageArea) {
                inSampleSize *= 2
            }
            return inSampleSize
        }

        private fun getImageRotation(imageFile: String, fromCamera: Boolean): Int {
            try {
                val exif = ExifInterface(imageFile)
                val exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    return 90
                } else if (exifOrientation == ExifInterface.ORIENTATION_UNDEFINED && fromCamera) {
                    return 90
                } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
                    return 180
                } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    return 270
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return 0
        }
    }
}
