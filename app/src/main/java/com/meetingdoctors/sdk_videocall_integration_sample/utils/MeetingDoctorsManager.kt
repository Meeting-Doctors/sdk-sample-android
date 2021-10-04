package com.meetingdoctors.sdk_videocall_integration_sample.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.meetingdoctors.chat.MeetingDoctorsClient
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.sdk_videocall_integration_sample.BuildConfig.*

import com.meetingdoctors.videocall.VideoCallClient
import com.meetingdoctors.videocall.VideoCallClient.hasAssignedCall
import com.meetingdoctors.videocall.VideoCallClient.hasProfessionalAssignedCall


/**
 * Created by Héctor Manrique on 26/8/21.
 */
object MeetingDoctorsManager : MeetingDoctorsClient.OnVideoCallRequest {

    fun initializeChatSDK(application: Application) {
        if (MeetingDoctorsClient.instance == null) {
            MeetingDoctorsClient.newInstance(
                application = application,
                apiKey = YOUR_API_KEY,
                targetEnvironment = BuildModeHelper.setCustomerSdkEnvironment(SDKCHAT_TARGET_ENVIRONMENT),
                isSharedPreferencesEncrypted = true,
                encryptionpassword = ENCRYPTION_PASSWORD,
                locale = null
            )

            MeetingDoctorsClient.instance?.setCollegiateNumbersVisibility(true)

        }
    }

    fun initVideocallSDK(activity: Activity, makeVideocallRequest: Boolean = false) {
        val installationGuid = MeetingDoctorsClient.instance?.installationGuid
        VideoCallClient.initialize(
            context = activity.applicationContext,
            apiKey = YOUR_API_KEY,
            installationGuid = installationGuid ?: "",
            targetEnvironment = BuildModeHelper.setVideocallSdkEnvironment(VIDEOCALL_TARGET_ENVIRONMENT),
            encryptionpassword = ENCRYPTION_PASSWORD,
            isSharedPreferencesEncrypted = true,
            listener = object : VideoCallClient.InitResponseListener {
                override fun onInitFailure(message: String?) {
                    Log.d("MeetingDoctorsManager", message ?: "error default")
                }

                override fun onInitSuccess() {
                    videocallLogin(activity, makeVideocallRequest)
                }
            })
    }

    private fun videocallLogin(activity: Activity, launchVideocallRequest: Boolean = false) {
        VideoCallClient.login(
            YOUR_TOKEN,
            object : VideoCallClient.LoginResponseListener {

                override fun onLoginSuccess() {
                    /* Usually you can start the video call from this point*/
                    if (launchVideocallRequest) makeVideoCallRequest(activity)
                }

                @Override
                override fun onLoginFailure(message: String?) {
                    /* Your code here */
                    Log.d("VIDEOCALL", message ?: "error default")
                }
            }
        )
    }

    private fun makeVideoCallRequest(activity: Activity) {
        VideoCallClient.openCall(activity,
            object : VideoCallClient.RequestCallResponseListener {

                override fun onRequestCallSuccess(roomId: String?, hash: String?) {
                    /* Your code here */
                    Log.d("VIDEOCALL", "request success")

                }

                override fun onRequestCallFailure(message: String?) {
                    /* Your code here */
                    Log.d("VIDEOCALL", "request failed $message")

                }
            })
    }

    fun setVideoCallRequestlistener() {
        MeetingDoctorsClient.instance?.setVideoCallRequestListener(this)
    }

    override fun perform1to1VideoCall(
        professionalHash: String?,
        doctorName: String?,
        context: Context?,
        videoCallRequestDoneListener: MeetingDoctorsClient.OnVideoCall1to1RequestDone?
    ) {
        Repository.instance?.getUserToken()?.let { userToken ->
            VideoCallClient.login(
                userToken,
                object : VideoCallClient.LoginResponseListener {
                    override fun onLoginSuccess() {
                        VideoCallClient.requestOneToOneCall(
                            context!!,
                            professionalHash!!,
                            doctorName,
                            object : VideoCallClient.RequestOneToOneCallListener {
                                override fun onRequestOneToOneCallSuccess(roomId: String) {
                                    Log.d("perform1to1VideoCall", "VideoCall Request Performed")
                                    videoCallRequestDoneListener?.performRequestDoneAction(roomId)
                                }

                                override fun onRequestOneToOneCallCancelledPrevious(roomId: String) {
                                    Log.d(
                                        "perform1to1VideoCall",
                                        "VideoCall Previous Request Cancelled"
                                    )
                                    videoCallRequestDoneListener?.performSendCancelledCallMessage(
                                        roomId,
                                        null
                                    )
                                }

                                override fun onRequestOneToOneCallFailure(message: String?) {
                                    Log.e("perform1to1VideoCall", message!!)
                                }
                            })
                    }

                    override fun onLoginFailure(s: String?) {
                        Toast.makeText(
                            context,
                            "Videocall Error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }

    override fun performCancelVideoCall(
        context: Context?,
        message: String?,
        videoCallRequestDoneListener: MeetingDoctorsClient.OnVideoCall1to1RequestDone?,
        doctorName: String?
    ) {
        VideoCallClient.requestCancelCallCustomer(context!!, doctorName!!, object :
            VideoCallClient.CancelCallResponseListener {
            override fun onCancelCallSuccess() {
                Log.i("VideoCallClient", "cancel Request Call SUCCESS")
                if (videoCallRequestDoneListener != null) {
                    videoCallRequestDoneListener.performRequestCancelledAction()
                    Toast.makeText(
                        context,
                        "Solicitud cancelada",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onCancelCallCancelledPrevious(roomId: String, hash: String?) {
                Log.i("VideocallClient", "cancel Request previous Call Success")
                videoCallRequestDoneListener?.performSendCancelledCallMessage(roomId, hash)
            }

            override fun onCancelCallFailure(message: String?) {
                Log.e("VideoCallClient", "cancel Request Call ERROR: \$message")
            }
        })
    }

    override fun hasProfessionalAssignedVideoCall(professionalHash: String?): Boolean {
        return hasAssignedCall() && hasProfessionalAssignedCall(professionalHash!!)
    }

}