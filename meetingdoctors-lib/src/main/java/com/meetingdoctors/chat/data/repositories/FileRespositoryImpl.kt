package com.meetingdoctors.chat.data.repositories

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import com.meetingdoctors.chat.data.FileMimeType
import com.meetingdoctors.chat.data.webservices.endpoints.FileApi
import com.meetingdoctors.chat.domain.repositories.FileRepository
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import java.io.*

internal class FileRespositoryImpl(private val fileApi: FileApi,
                                   private val context: Context) : FileRepository {


    private val byteArraySize = 4096

    override fun getFile(fileUrl: String): Single<ResponseBody> {

        return fileApi.downloadFileWithDynamicUrl(fileUrl).flatMap {
            if (it.isSuccessful) {
                Single.just(it.body())
            } else {
                Single.error(Throwable("Unable to download File"))
            }
        }

    }

    override fun writeResponseBodyToDisk(body: ResponseBody, fileName: String, fileMimeType: FileMimeType): Completable {

        return Completable.create { emitter ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.Files.FileColumns.MIME_TYPE, fileMimeType.type)
                    put(MediaStore.Files.FileColumns.IS_PENDING, 1)
                }

                val collection =
                        MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                val fileUri = context.contentResolver.insert(collection, values)
                val fileInputStream = body.byteStream()
                fileUri?.let {
                    context.contentResolver.openFileDescriptor(fileUri, "w").use { parcelFileDescriptor ->
                        ParcelFileDescriptor.AutoCloseOutputStream(parcelFileDescriptor)
                                .write(fileInputStream.readBytes())
                    }
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    context.contentResolver.update(fileUri, values, null, null)
                    emitter.onComplete()
                } ?: emitter.onError(Throwable("Unnable to save file"))

            } else {

                val pathToSave =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
                val path = File(pathToSave)
                if (!path.exists()) {
                    path.mkdirs()
                }
                val fileUri = Uri.fromFile(path)
                val file = File(pathToSave, fileName)
                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null

                try {

                    val fileReader = ByteArray(byteArraySize)
                    var fileSizeDownloaded: Long = 0
                    inputStream = body.byteStream()
                    outputStream = FileOutputStream(file)

                    while (true) {
                        val read = inputStream!!.read(fileReader)

                        if (read == -1) {
                            break
                        }

                        outputStream.write(fileReader, 0, read)
                        fileSizeDownloaded += read.toLong()
                    }

                    outputStream.flush()

                    inputStream.close()
                    outputStream.close()

                    Intent().also { scanIntent ->
                        scanIntent.action = Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
                        scanIntent.data = fileUri
                        context.sendBroadcast(scanIntent)
                    }


                    emitter.onComplete()

                } catch (e: IOException) {
                    inputStream?.close()
                    outputStream?.close()
                    emitter.onError(e)
                }

            }
        }
    }

}