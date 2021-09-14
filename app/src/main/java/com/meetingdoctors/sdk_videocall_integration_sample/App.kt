package com.meetingdoctors.sdk_videocall_integration_sample

import androidx.multidex.MultiDexApplication
import com.meetingdoctors.sdk_videocall_integration_sample.utils.MeetingDoctorsManager


class App: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        MeetingDoctorsManager.initializeChatSDK(this)
    }
}