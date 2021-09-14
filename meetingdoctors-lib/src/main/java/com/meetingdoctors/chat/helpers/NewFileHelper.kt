@file:JvmName("NewFileHelper")
package com.meetingdoctors.chat.helpers

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore

class NewFileHelper() {

    fun getRealPathFromURI(context: Context, uri: Uri): String? {

        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA
                )

        val cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null,
                null)

        val idColumn = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)!!

        return if (cursor == null) {
            uri.path
        } else {
            cursor.moveToFirst()
            val id = cursor.getLong(idColumn)
            val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            cursor.close()

            contentUri.path
        }
    }
}