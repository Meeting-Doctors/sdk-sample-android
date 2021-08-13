package com.meetingdoctors.sdk_videocall_integration_sample

import androidx.multidex.MultiDexApplication
import com.meetingdoctors.chat.MeetingDoctorsClient
import com.meetingdoctors.chat.MeetingDoctorsClient.Companion.instance
import com.meetingdoctors.chat.data.webservices.CustomerSdkBuildMode


class App: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        MeetingDoctorsClient.newInstance(application = this,
            apiKey = BuildConfig.YOUR_API_KEY,
            targetEnvironment = CustomerSdkBuildMode.DEV,
            isSharedPreferencesEncrypted = true,
            encryptionpassword = "abcdefg",
            locale = null)

        instance!!.setCollegiateNumbersVisibility(true)
    }
}