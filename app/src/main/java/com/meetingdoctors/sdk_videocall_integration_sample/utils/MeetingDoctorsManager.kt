package com.meetingdoctors.sdk_videocall_integration_sample.utils

import android.app.Activity
import android.app.Application
import android.util.Log
import com.meetingdoctors.chat.MeetingDoctorsClient
import com.meetingdoctors.chat.data.webservices.CustomerSdkBuildMode
import com.meetingdoctors.sdk_videocall_integration_sample.BuildConfig
import com.meetingdoctors.videocall.VideoCallClient
import com.meetingdoctors.videocall.api.VideoCallBuildMode


/**
 * Created by Héctor Manrique on 26/8/21.
 */
object MeetingDoctorsManager {

    fun initializeChatSDK(application: Application) {
        if (MeetingDoctorsClient.instance == null) {
            MeetingDoctorsClient.newInstance(
                application = application,
                apiKey = BuildConfig.YOUR_API_KEY,
                targetEnvironment = CustomerSdkBuildMode.DEV,
                isSharedPreferencesEncrypted = false,
                encryptionpassword = "",
                locale = null
            )

            MeetingDoctorsClient.instance?.setCollegiateNumbersVisibility(true)

        }
    }

    fun initVideocallSDK(activity: Activity) {
        val installationGuid = MeetingDoctorsClient.instance?.installationGuid
        VideoCallClient.initialize(
            context = activity.applicationContext,
            apiKey = BuildConfig.YOUR_API_KEY,
            installationGuid = installationGuid ?: "",
            targetEnvironment = VideoCallBuildMode.DEV,
            encryptionpassword = "",
            isSharedPreferencesEncrypted = false,
            listener = object : VideoCallClient.InitResponseListener {
                override fun onInitFailure(message: String?) {
                    Log.d("MeetingDoctorsManager", message ?: "error default")
                }

                override fun onInitSuccess() {
                    videocallLogin(activity)
                }
            })
    }

    private fun videocallLogin(activity: Activity) {
        VideoCallClient.login(
            BuildConfig.YOUR_TOKEN,
            object : VideoCallClient.LoginResponseListener {

                override fun onLoginSuccess() {
                    /* Usually you can start the video call from this point*/
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

                @Override
                override fun onLoginFailure(message: String?) {
                    /* Your code here */
                    Log.d("VIDEOCALL", message ?: "error default")
                }
            }
        )
    }

}