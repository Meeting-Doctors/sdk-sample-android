@file:JvmName("FileUtils")

package com.meetingdoctors.chat.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

private const val tag = "FileUtils"

suspend fun Activity.compressImageFile(
        path: String,
        shouldOverride: Boolean = true,
        uri: Uri
): String {
    return withContext(Dispatchers.IO) {
        var scaledBitmap: Bitmap? = null

        try {
            val (hgt, wdt) = getImageHgtWdt(uri)
            try {
                val bm = getBitmapFromUri(uri)
                Log.d(tag, "original bitmap height${bm?.height} width${bm?.width}")
                Log.d(tag, "Dynamic height$hgt width$wdt")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Part 1: Decode image
            val unscaledBitmap = decodeFile(this@compressImageFile, uri, wdt, hgt, ScalingLogic.FIT)
            if (unscaledBitmap != null) {
                if (!(unscaledBitmap.width <= 800 && unscaledBitmap.height <= 800)) {
                    // Part 2: Scale image
                    scaledBitmap = createScaledBitmap(unscaledBitmap, wdt, hgt, ScalingLogic.FIT)
                } else {
                    scaledBitmap = unscaledBitmap
                }
            }

            // Store to tmp file
            val mFolder = File("$filesDir/Images")
            if (!mFolder.exists()) {
                mFolder.mkdir()
            }

            val tmpFile = File(mFolder.absolutePath, "IMG_${getTimestampString()}.png")

            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(tmpFile)
                scaledBitmap?.compress(
                        Bitmap.CompressFormat.PNG,
                        getImageQualityPercent(tmpFile),
                        fos
                )
                fos.flush()
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()

            } catch (e: Exception) {
                e.printStackTrace()
            }

            var compressedPath = ""
            if (tmpFile.exists() && tmpFile.length() > 0) {
                compressedPath = tmpFile.absolutePath
                if (shouldOverride) {
                    val srcFile = File(path)
                    val result = tmpFile.copyTo(srcFile, true)
                    Log.d(tag, "copied file ${result.absolutePath}")
                    Log.d(tag, "Delete temp file ${tmpFile.delete()}")
                }
            }

            scaledBitmap?.recycle()

            return@withContext if (shouldOverride) path else compressedPath
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return@withContext ""
    }

}

@Throws(IOException::class)
fun Context.getBitmapFromUri(uri: Uri, options: BitmapFactory.Options? = null): Bitmap? {
    val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
    val fileDescriptor = parcelFileDescriptor?.fileDescriptor
    val image: Bitmap? = if (options != null)
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options)
    else
        BitmapFactory.decodeFileDescriptor(fileDescriptor)
    parcelFileDescriptor?.close()
    return image
}

fun getTimestampString(): String {
    val date = Calendar.getInstance()
    return SimpleDateFormat("yyyy MM dd hh mm ss", Locale.US).format(date.time).replace(" ", "")
}

fun Context?.isImageFile(fileUri: Uri?): Boolean {
    return if (fileUri != null) {
        val fileName = getFileNameFromUri(this, fileUri)
        fileName != null && fileName.toLowerCase().matches(Regex(".*\\.(png|jpg|jpeg|gif|bmp|webp)$"))
    } else {
        false
    }
}

fun getHumanReadableFileSize(bytes: Long): String? {
    val unit = 1000
    if (bytes < unit) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
    val pre = "kMGTPE"[exp - 1].toString() + ""
    return String.format(Locale.getDefault(),
            "%.1f %sB",
            bytes / Math.pow(unit.toDouble(),
                    exp.toDouble()),
            pre)
}

/*fun getRealPathFromURI(context: Context, uri: Uri): String? {
    val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
    val cursor = context.contentResolver.query(uri,
            projection,
            null,
            null,
            null,
            null)
    if (cursor == null) {
        return uri.path
    } else {
        cursor.moveToFirst()
        var idx = cursor.getColumnIndex("filePath")
        if (idx != -1) {
            return cursor.getString(idx)
        }
        idx = cursor.getColumnIndex("_data")
        if (idx != -1) {
            return cursor.getString(idx)
        }
        cursor.close()
    }
    return null
}*/
//MediaStore.File.FileColumns.Data is deprecated, this is a new method to aquire the real path
fun getRealPathFromURI(
        context: Context,
        uri: Uri?): String? {

    val contentResolver = context.contentResolver ?: return null

    // Create file path inside app's data dir
    val filePath = (context.applicationInfo.dataDir + File.separator
            + System.currentTimeMillis())
    val file = File(filePath)
    try {
        val inputStream = contentResolver.openInputStream(uri!!) ?: return null
        val outputStream: OutputStream = FileOutputStream(file)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()
    } catch (ignore: IOException) {
        return null
    }
    return file.absolutePath
}

fun getFileNameFromUri(context: Context?, uri: Uri): String? {
    val cursor = context?.contentResolver?.query(uri, null, null, null, null)
    if (cursor == null) {
        return File(uri.path).name
    } else {
        cursor.moveToFirst()
        var idx = cursor.getColumnIndex("filePath")
        if (idx != -1) {
            return File(cursor.getString(idx)).name
        }
        idx = cursor.getColumnIndex("_display_name")
        if (idx != -1) {
            return cursor.getString(idx)
        }
        idx = cursor.getColumnIndex("_data")
        if (idx != -1) {
            return File(cursor.getString(idx)).name
        }
        cursor.close()
    }
    return null
}

fun readFileFromUri(context: Context, uri: Uri?): ByteArray? {
    try {
        val inputStream = context.contentResolver.openInputStream(uri!!)
        val buffer = ByteArrayOutputStream()
        var nRead: Int
        val data = ByteArray(16384)
        while (inputStream!!.read(data, 0, data.size).also { nRead = it } != -1) {
            buffer.write(data, 0, nRead)
        }
        buffer.flush()
        return buffer.toByteArray()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return null
}

fun openFile(context: Context, fileUrl: String?, fileName: String?) {
    if (fileName != null && fileName.contains(".")) {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            val chunks = fileName.split("\\.".toRegex()).toTypedArray()
            val fileExtension = chunks[chunks.size - 1]
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)
            intent.setDataAndType(Uri.parse(fileUrl), mimeType)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
        } catch (e: java.lang.Exception) {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(fileUrl)
            context.startActivity(intent)
        }
    } else {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse(fileUrl)
        context.startActivity(intent)
    }
}
