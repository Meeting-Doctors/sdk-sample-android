package com.meetingdoctors.chat.views.relationships

import android.content.Context
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.data.repositories.CustomerResponseListener
import kotlinx.android.synthetic.main.dialog_relationships_invitation_code.*

class InvitationCodeDialog : InvitationCodeAdapter {

    override fun showInvitationCodeDialog(context: Context, actionListener: InvitationCodeDialogActions?) {

        val invitationCodeDialog = InvitationCodeDialogAdapter.generateDialog(context,
                R.layout.dialog_relationships_invitation_code,
                actionListener)
        invitationCodeDialog.show()

        invitationCodeDialog.invitation_code_dialog_code_accept_button.setOnClickListener {
            val code = invitationCodeDialog.invitation_code_dialog_code_text.text.toString()

            Repository.instance?.sendInvitationCode(code, object: CustomerResponseListener {
                override fun onSuccessResponse() {
                    actionListener?.sendInvitationCode(code)
                    Toast.makeText(context, "Se ha enviado el codigo de invitación", Toast.LENGTH_LONG).show()
                }

                override fun onErrorResponse(error: String) {
                    super.onErrorResponse(error)
                    Toast.makeText(context, "Ha ocurrido un error enviando el codigo de invitacion", Toast.LENGTH_LONG).show()
                }
            })

            invitationCodeDialog.dismiss()
        }

        invitationCodeDialog.invitation_code_dialog_code_cancel_button.setOnClickListener {
            invitationCodeDialog.dismiss()
            actionListener?.dismissInvitationCodeDialog()
        }
    }
}

private object InvitationCodeDialogAdapter {
    fun generateDialog(context: Context, @LayoutRes layoutId: Int?, invitationCodeDialogListener: InvitationCodeDialogActions?): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.setView(layoutId!!)

        val dialog = builder.create()
//        dialog.setStyle(STYLE_NO_TITLE, 0)
        dialog.setCancelable(false)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setGravity(Gravity.CENTER)
//        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCanceledOnTouchOutside(false)

        val metrics = context.resources.displayMetrics

        val windowParams = WindowManager.LayoutParams()
        windowParams.copyFrom(dialog?.window?.attributes)
        windowParams.width = (metrics.widthPixels * 0.94).toInt()
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog?.window?.attributes = windowParams

        return dialog
    }
}