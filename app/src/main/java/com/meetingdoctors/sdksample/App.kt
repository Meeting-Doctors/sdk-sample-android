package com.meetingdoctors.sdksample

import androidx.multidex.MultiDexApplication
import com.meetingdoctors.sdksample.utils.MeetingDoctorsManager


class App: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        MeetingDoctorsManager.initializeChatSDK(this)
    }
}