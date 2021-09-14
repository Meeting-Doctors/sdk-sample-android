package com.meetingdoctors.chat.fcm

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.meetingdoctors.chat.MeetingDoctorsClient
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.domain.entities.Setup
import com.meetingdoctors.chat.helpers.NpsHelper.storeNpsStatusCompleted
import com.meetingdoctors.chat.helpers.SystemHelper.Companion.getApplicationName
import org.json.JSONObject
import java.io.IOException

/**
 * Created by Héctor Manrique on 4/15/21.
 */

class MDFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        onMessageReceived(this, remoteMessage, null)
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.i("onNewToken", "New token generated from FIREBASE:$s")
    }

    companion object {
        const val NOTIFICATION_TYPE_PENDING_MESSAGES = 1234
        const val NOTIFICATION_TYPE_NPS_REQUEST = 5678
        const val NOTIFICATION_CHANNEL_ID = "meetingdoctorsChatId"
        const val NPS_REQUEST_ID_KEY = "npsRequestIdKey"
        const val ROOM_ID_KEY = "roomId"
        const val DOCTOR_USER_HASH_KEY = "doctorUserHash"
        const val NAVIGATION_TO_CHAT_ACTIVITY_KEY = "navigationToChat"
        fun unregisterToken() {
            Log.i("FCM", "unregisterToken()")
            val thread = Thread(Runnable {
                try {
                    FirebaseMessaging.getInstance().deleteToken()
                    Log.i("FCM", "Delete InstanceId successfull")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            })
            thread.start()
        }

        private fun sendLocalBroadcast(context: Context, roomId: Int?): Boolean {
            val intent = Intent(context.getString(R.string.meetingdoctors_local_broadcast_gcm_message_received))
            if (roomId != null) {
                val bundle = Bundle()
                bundle.putInt(ROOM_ID_KEY, roomId)
                intent.putExtras(bundle)
            }
            return LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

        @JvmOverloads
        fun showPendingMessagesNotification(context: Context,
                                            roomId: Int? = null,
                                            message: String? = null,
                                            activityToLaunch: Class<out Activity?>? = null) {
            Log.d("FCM", "showPendingMessagesNotification() roomId[$roomId]")

            // build notification
            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        context.getString(R.string.meetingdoctors_notification_channel_medical_chat), NotificationManager.IMPORTANCE_HIGH)
                mNotificationManager.createNotificationChannel(mChannel)
            }
            val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(getNotificationIconResourceId(context))
                    .setColor(context.resources.getColor(R.color.meetingdoctors_notification_color))
                    .setContentTitle(getApplicationName(context))
                    .setContentText(message
                            ?: context.getString(R.string.meetingdoctors_push_unread_messages))
                    .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.meetingdoctors_push_unread_messages)))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (roomId != null) {
                val doctor = Repository.instance!!.getDoctorByRoomId(roomId)
                if (doctor?.avatar != null) {
                    // set professional name as title if available
                    doctor.name?.let {
                        notificationBuilder.setContentTitle(it)
                    }

                    // set the action to take when a user taps the notification
                    val resultIntent = Intent(context, activityToLaunch)
                    resultIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    resultIntent.putExtra(DOCTOR_USER_HASH_KEY, doctor.hash)
                    resultIntent.putExtra(NAVIGATION_TO_CHAT_ACTIVITY_KEY, true)
                    val resultPendingIntent = PendingIntent.getActivity(context,
                            System.currentTimeMillis().toInt(),
                            resultIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT)
                    notificationBuilder.setContentIntent(resultPendingIntent)

                    // set notification icon
                    val mainHandler = Handler(context.mainLooper)
                    mainHandler.post {
                        Glide.with(context.applicationContext).asBitmap().load(doctor.avatar)
                                .apply(RequestOptions.circleCropTransform()).into(object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                        // set the action to take when a user taps the notification
                                        notificationBuilder.setLargeIcon(resource)

                                        // show the notification
                                        notificationManager.cancel(NOTIFICATION_TYPE_PENDING_MESSAGES)
                                        notificationManager.notify(NOTIFICATION_TYPE_PENDING_MESSAGES + roomId, notificationBuilder.build())
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {
                                        TODO("Not yet implemented")
                                    }
                                })
                    }
                    return
                }
            }

            // set the action to take when a user taps the notification
            val resultIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            val resultPendingIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), resultIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            notificationBuilder.setContentIntent(resultPendingIntent)
            // show the notification
            notificationManager.notify(NOTIFICATION_TYPE_PENDING_MESSAGES, notificationBuilder.build())
        }

        fun showOpenUrlNotification(
            context: Context,
            title: String?,
            message: String?,
            url: String?
        ) {
            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        context.getString(R.string.meetingdoctors_notification_channel_medical_chat), NotificationManager.IMPORTANCE_HIGH)
                mNotificationManager.createNotificationChannel(mChannel)
            }

            // build notification
            val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(getNotificationIconResourceId(context))
                    .setColor(context.resources.getColor(R.color.meetingdoctors_notification_color))
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)

            // set the action to take when a user taps the notification
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            val resultPendingIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT)
            notificationBuilder.setContentIntent(resultPendingIntent)

            // show the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify((Math.random() * 1000).toInt(), notificationBuilder.build())
        }

        fun showNpsRequestNotification(context: Context, message: String?, activityToLaunch: Class<out Activity?>?) {
            Log.d("FCM", "showNpsRequestNotification()")

            // build notification
            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        context.getString(R.string.meetingdoctors_notification_channel_medical_chat),
                        NotificationManager.IMPORTANCE_HIGH)
                mNotificationManager.createNotificationChannel(mChannel)
            }
            val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(getNotificationIconResourceId(context))
                    .setColor(ContextCompat.getColor(context, R.color.meetingdoctors_notification_color))
                    .setContentTitle(getApplicationName(context))
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.meetingdoctors_push_unread_messages)))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)

            // set the action to take when a user taps the notification
            val resultIntent = Intent(context, activityToLaunch)
            val resultPendingIntent = PendingIntent.getActivity(context,
                    System.currentTimeMillis().toInt(),
                    resultIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT)
            notificationBuilder.setContentIntent(resultPendingIntent)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify((Math.random() * 1000).toInt(), notificationBuilder.build())
        }

        fun hidePendingMessagesNotification(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_TYPE_PENDING_MESSAGES)
        }

        fun hidePendingMessagesNotificationByRoom(context: Context, room: Int) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_TYPE_PENDING_MESSAGES + room)
        }

        private fun getNotificationIconResourceId(context: Context): Int {
            var notificationIconResourceId = 0
            try {
                val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                notificationIconResourceId = applicationInfo.metaData.getInt("com.meetingdoctors.chat.notification_icon")
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            if (notificationIconResourceId == 0) {
                notificationIconResourceId = R.drawable.mediquo_default_notification_icon
            }
            return notificationIconResourceId
        }

        private fun processSilentPush() {
            Repository.instance?.getSetup(object : Repository.GetSetupResponseListener {
                override fun onResponse(error: Throwable?, setup: Setup?) {}
            })
        }

        fun onMessageReceived(context: Context, remoteMessage: RemoteMessage, activityToLaunch: Class<out Activity?>?): Boolean {
            Log.i("FCM", "onMessageReceived()")
            var processed = false
            try {
                val json = remoteMessage.data["data"]
                Log.i("FCM", "onMessageReceived() json[$json]")
                val data = JSONObject(json)
                if (data.has("module") && data.getString("module") == "consultations") {
                    processed = true
                    when (data.getString("type").toLowerCase()) {
                        "message_created" -> {
                            if (data.has("room_id")) {
                                val roomId = data.getInt("room_id")
                                Log.i("FCM", "onMessageReceived() Message push received roomId[$roomId]")
                                // Skip notification if there are listeners
                                if (!sendLocalBroadcast(context, roomId)) {
                                    if (Repository.instance?.isAuthenticated() == true) {
                                        Log.i("FCM", "onMessageReceived() Notification shown")
                                        val message = JSONObject(data.getString("message"))
                                        showPendingMessagesNotification(context, roomId, message.getString("body"), activityToLaunch)
                                    }
                                } else {
                                    Log.i("FCM", "onMessageReceived() Local broadcast listeners notified")
                                }
                            }
                        }
                        "silent" -> {
                            Log.i("FCM", "onMessageReceived() Silent push received")
                            processSilentPush()
                        }
                        "open_url" -> {
                            if (data.has("message") || data.has("url")) {
                                val messageObject = JSONObject(data.getString("message"))
                                val message = messageObject.getString("body")
                                val title = messageObject.getString("title")
                                val url = data.getString("url")
                                Log.i("FCM", "onMessageReceived() OpenUrl push received message[$message] url[$url]")
                                showOpenUrlNotification(context, title, message, url)
                            }
                        }
                        "rating_request" -> {
                            Log.i("FCM", "onMessageReceived() RatingRequest push received")
                            MeetingDoctorsClient.instance?.ratingRequestListener?.let {
                                it.onActiveRatingRequest()
                            }
                        }
                        "nps" -> {
                            Log.i("NPS", "onMessageReceived() NPS push received")
                            var message: String? = context.getString(R.string.nps_pending_actions_notification)
                            if (data.has("message")) {
                                message = data.getString("message")
                            }
                            showNpsRequestNotification(context, message, activityToLaunch)
                            storeNpsStatusCompleted(false)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.i("FCM", "onMessageReceived() Unknown message format")
            }
            return processed
        }
    }
}