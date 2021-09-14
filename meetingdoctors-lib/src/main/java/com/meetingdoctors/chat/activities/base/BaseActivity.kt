package com.meetingdoctors.chat.activities.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.meetingdoctors.chat.BuildConfig
import com.meetingdoctors.chat.MeetingDoctorsClient
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.ChatActivity
import com.meetingdoctors.chat.activities.DoctorProfileActivity
import com.meetingdoctors.chat.activities.ImageViewerActivity
import com.meetingdoctors.chat.activities.medicalhistory.*
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.data.webservices.endpoints.RequestMessageApi
import com.meetingdoctors.chat.data.webservices.getConsultationsCustomerServer
import com.meetingdoctors.chat.data.webservices.toolkit.OkHttpClientFactory
import com.meetingdoctors.chat.net.ServerInterface
import com.meetingdoctors.mdsecure.debugging.MDSecureRuntimeDebuggingInspector.tamperingDebuggerInspectMatch
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Created by Héctor Manrique on 4/15/21.
 */

open class BaseActivity : LocaleManagerActivity() {

    override fun onResume() {
        super.onResume()
        if (Repository.instance?.isAuthenticated() == false) {
            finish()
            return
        }
        Repository.instance?.initialize(object : ServerInterface.ResponseListener {
            override fun onResponse(error: Throwable?, statusCode: Int, data: Any?) {
                if (!isFinishing) onReady()
            }
        })
    }

    open fun onReady() {
//        launchSuspiciousBehaviorWatchdog();
    }

    override fun onBackPressed() {
        if (isFinishing) return
        super.onBackPressed()
        overridePendingTransition(R.anim.mediquo_hold, R.anim.mediquo_right_side_out)

        // lib shouldn't be the last activity
        if (isTaskRoot) {
            val intent: Intent? = if (MeetingDoctorsClient.instance?.returnIntent != null) {
                MeetingDoctorsClient.instance?.returnIntent
            } else {
                packageManager.getLaunchIntentForPackage(packageName)
            }
            intent?.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
    }

    private fun launchSuspiciousBehaviorWatchdog() {
        if (BuildConfig.BUILD_TYPE === "release" &&
                tamperingDebuggerInspectMatch(this)) {
            showAntiDebugTamperingDialog(this@BaseActivity)
        }
    }

    private fun showAntiDebugTamperingDialog(activity: Activity) {
        val builder = AlertDialog.Builder(activity)
        val htmlMessage = HtmlCompat.fromHtml(getString(R.string.security_unauthorized_behavior_detected),
                HtmlCompat.FROM_HTML_MODE_LEGACY)
        builder.setMessage(htmlMessage)
        builder.setPositiveButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
            activity.finish()
        }
        builder.setOnDismissListener {
            activity.finish()
        }
        val rootDeviceDialog = builder.create()
        rootDeviceDialog.show()
        (rootDeviceDialog.findViewById<View>(android.R.id.message) as TextView).movementMethod = LinkMovementMethod.getInstance()
    }

    companion object {

        fun launchChat(context: Context, doctorUserHash: String) {
            launchChat(context, doctorUserHash, null, null)
        }

        fun launchChatAndSendMessage(context: Context, doctorUserHash: String, message: String?) {
            launchChat(context, doctorUserHash, message, null)
        }

        fun launchChatAndReceiveMessage(context: Context, doctorUserHash: String, message: String?) {
            launchChat(context, doctorUserHash, null, message)
        }

        @SuppressLint("CheckResult")
        fun receiveMessage(context: Context?, doctorUserHash: String?, incomingMessage: String?) {
            val repository = Repository.instance
            if (incomingMessage != null && repository != null) {
                val api = Retrofit.Builder()
                        .baseUrl(getConsultationsCustomerServer(repository.getEnvironmentTarget()))
                        .client(OkHttpClientFactory(repository).createOkHttpClient())
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .addConverterFactory(MoshiConverterFactory.create())
                        .build()
                        .create(RequestMessageApi::class.java)
                api.requestMessage(incomingMessage, doctorUserHash!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ responseBody ->
                            Log.i("RequestMessage",
                                    "Responsebody : " + responseBody.string())
                        })
                        { throwable ->
                            Log.e("RequestMessage",
                                    "Error throwable: " + throwable.localizedMessage.toString())
                        }
            }
        }

        private fun launchChat(context: Context,
                               doctorUserHash: String,
                               outgoingMessage: String?,
                               incomingMessage: String?) {

            if (Repository.instance?.getUserData()?.banned != null
                    && Repository.instance?.getUserData()?.banned == 1L) {
                showBannedDialog(context)
                return
            }
            val intent = ChatActivity.getIntent(context,
                    doctorUserHash,
                    outgoingMessage,
                    incomingMessage)
            if (context is Activity) {
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            } else {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }

        fun launchImageViewer(context: Context, imagePath: String?) {
            val intent = Intent(context, ImageViewerActivity::class.java)
            intent.putExtra("imagePath", imagePath)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }

        fun launchDoctorProfile(context: Context, doctorUserHash: String?) {
            val intent = Intent(context, DoctorProfileActivity::class.java)
            intent.putExtra("doctorUserHash", doctorUserHash)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }

        fun launchMedicalHistory(context: Context) {
            val intent = Intent(context, MedicalHistoryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }

        fun launchAllergies(context: Context) {
            val intent = Intent(context, AllergiesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }

        fun launchAllergy(context: Context, allergyId: Long) {
            val intent = Intent(context, AllergyDetailActivity::class.java)
            intent.putExtra("allergy_id", allergyId)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }

        fun launchNewAllergy(context: Context) {
            val intent = Intent(context, AllergyDetailActivity::class.java)
            intent.putExtra("allergy_id", 0L)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }

        fun launchDiseases(context: Context) {
            val intent = Intent(context, DiseasesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }

        fun launchDisease(context: Context, diseaseId: Long) {
            val intent = Intent(context, DiseaseDetailActivity::class.java)
            intent.putExtra("disease_id", diseaseId)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }

        fun launchNewDisease(context: Context) {
            val intent = Intent(context, DiseaseDetailActivity::class.java)
            intent.putExtra("disease_id", 0L)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }

        fun launchMedications(context: Context) {
            val intent = Intent(context, MedicationsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }

        fun launchReports(context: Context) {
            val intent = Intent(context, ReportsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }

        fun launchReferrals(context: Context) {
            val intent = Intent(context, ReferralsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }

        fun launchMedication(context: Context, medicationId: Long) {
            val intent = Intent(context, MedicationDetailActivity::class.java)
            intent.putExtra("medication_id", medicationId)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }

        fun launchNewMedication(context: Context) {
            val intent = Intent(context, MedicationDetailActivity::class.java)
            intent.putExtra("medication_id", 0L)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }

        fun showBannedDialog(context: Context?) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.meetingdoctors_banned_dialog_title)
            builder.setMessage(R.string.meetingdoctors_banned_dialog_body)
            builder.setPositiveButton(R.string.meetingdoctors_banned_dialog_button) { dialog, _ ->
                dialog.dismiss()
            }
            val bannerDialog = builder.create()
            bannerDialog.show()
        }
    }
}
