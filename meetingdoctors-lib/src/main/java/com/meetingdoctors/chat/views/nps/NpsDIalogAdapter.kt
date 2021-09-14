package com.meetingdoctors.chat.views.nps

import android.content.Context

interface NpsRatingDialogActions
{
    fun close() {}
    fun setRating() {}
    fun submitPoll() {}
}

internal interface NpsDialogAdapter {
    fun showNpsDialog(context: Context, actionListener: NpsRatingDialogActions?)
}