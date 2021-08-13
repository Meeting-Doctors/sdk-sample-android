package com.meetingdoctors.sdk_videocall_integration_sample.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.meetingdoctors.chat.MeetingDoctorsClient.Companion.instance
import com.meetingdoctors.sdk_videocall_integration_sample.ui.activity.MainActivity

class VideocallFirebaseMessaging: FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.i("FCM", "onMessageReceived() Push received on app")
        super.onMessageReceived(p0)

        instance!!.onFirebaseMessageReceived(
            p0,
            MainActivity::class.java
        )
    }

    override fun onNewToken(p0: String) {
        Log.i("FCM", "onNewToken() New token received on app")
        super.onNewToken(p0)
        instance!!.onNewTokenReceived(p0)
    }
}