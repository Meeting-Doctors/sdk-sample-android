package com.meetingdoctors.chat.views.relationships

import android.content.Context
import com.meetingdoctors.chat.views.nps.NpsRatingDialogActions


interface InvitationCodeDialogActions
{
    fun sendInvitationCode(code: String) {}
    fun dismissInvitationCodeDialog() {}
}

interface InvitationCodeAdapter {
    fun showInvitationCodeDialog(context: Context, actionListener: InvitationCodeDialogActions?)
}