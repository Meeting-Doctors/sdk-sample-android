package com.meetingdoctors.sdksample.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.meetingdoctors.chat.MeetingDoctorsClient.Companion.instance
import com.meetingdoctors.sdksample.ui.activity.MainActivity
import com.meetingdoctors.videocall.fcm.MDVideoCallFirebaseMessagingService.Companion.onMessageReceived

class VideocallFirebaseMessaging : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.i("FCM", "onMessageReceived() Push received on app")
        super.onMessageReceived(remoteMessage)

        try {
            instance?.onFirebaseMessageReceived(
                remoteMessage,
                MainActivity::class.java
            )
        } catch (e: Exception) {
            Log.e("MDFirebaseMessage", "Firebase Exception : " + e.message)
        }

        try {
            onMessageReceived(remoteMessage, this.applicationContext)
        } catch (e: Exception) {
            Log.e("VCFirebaseMessage", "Firebase Exception : " + e.message)
        }
    }

    override fun onNewToken(token: String) {
        Log.i("FCM", "onNewToken() New token received on app")
        super.onNewToken(token)
    }
}