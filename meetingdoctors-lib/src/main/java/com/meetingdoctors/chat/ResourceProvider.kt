package com.meetingdoctors.chat

import android.content.Context
import androidx.annotation.StringRes

class ResourceProvider(private val context: Context)  {

    fun getString(@StringRes stringResourceId: Int): String {
        return context.getString(stringResourceId)
    }

    fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String {
        return context.getString(resId, *formatArgs)
    }
}