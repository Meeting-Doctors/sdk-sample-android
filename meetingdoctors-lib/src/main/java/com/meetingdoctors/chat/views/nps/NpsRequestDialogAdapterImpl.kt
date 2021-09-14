@file:JvmName("NpsDialogAdapterImpl")
package com.meetingdoctors.chat.views.nps

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.Keep
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.helpers.NpsHelper
import kotlinx.android.synthetic.main.dialog_nps.*

internal class NpsRequestDialogAdapterImpl: NpsDialogAdapter {
    override fun showNpsDialog(context: Context, actionListener: NpsRatingDialogActions?) {

        val npsDialog = DialogFactory.generateDialog(context, R.layout.dialog_nps)

        npsDialog.show()

        npsDialog.nps_dialog_rate_button.isClickable = true
        npsDialog.nps_dialog_rate_button?.setOnClickListener {

            val npsRate : String = npsDialog.nps_dialog_indicator_rating_bar?.getCurrentRateSelected() ?: NO_RATING_SELECTED.toString()
            val descriptionText = npsDialog.nps_dialog_questionay_edit_text.text?.toString() ?: ""
            when (npsDialog.nps_dialog_indicator_rating_bar.isBarRated()) {
                true -> {
                    actionListener?.apply {
                        this.setRating()
                        NpsHelper.storeNpsStatusCompleted(true)
                        Repository.instance?.storeNpsRequest(npsRate, descriptionText, 0, actionListener)
                        npsDialog.dismiss()
                    }
                }
                false -> { Toast.makeText(context, "Por favor, selecciona una puntuación a nuestro servicio.",
                        Toast.LENGTH_LONG).show() }
            }
        }

        npsDialog.nps_dialog_close_button?.setOnClickListener {
            npsDialog.dismiss()
        }
    }

    private object DialogFactory {

        fun generateDialog(context: Context, @LayoutRes layoutId: Int?): AlertDialog {
            val builder = AlertDialog.Builder(context, R.style.meetingdoctors_nps_dialog)
            builder.setView(layoutId!!)
            val dialog = builder.create()
//            setStyle(STYLE_NO_TITLE, 0)
            dialog.setCancelable(false)
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog?.window?.setGravity(Gravity.CENTER)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
}

 internal const val NO_RATING_SELECTED = -1