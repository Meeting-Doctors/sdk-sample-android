package com.meetingdoctors.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.*


/**
 * Created by Héctor Manrique on 5/28/21.
 */

class Tracking(context: Context) {
    private val EVENT_TYPE_TRACKING = "eventType"
    private val ROOM_ID_TRACKING = "roomId"
    private val PROFESSIONAL_HASH_TRACKING = "professionalHash"
    private val SPECIALITY_TRACKING = "speciality"
    private val CHAT_VIEW_TRACKING = "ChatView"
    private val PROFESSIONAL_PROFILE_VIEW_TRACKING = "ProfessionalProfileView"
    private val CHAT_MESSAGE_SENT_TRACKING = "ChatMessageSent"
    private val CHAT_MESSAGE_RECEIVED_TRACKING = "ChatMessageReceived"
    private val MESSAGE_TYPE_TRACKING = "messageType"
    private val MESSAGE_ID_TRACKING = "messageId"
    private val MESSAGE_TRACKING = "message"
    private val context: Context = context

    fun viewChat(roomId: Int, professionalHash: String, speciality: String) {
        val parameters: MutableMap<String, Any> = HashMap()
        parameters[EVENT_TYPE_TRACKING] = CHAT_VIEW_TRACKING
        parameters[ROOM_ID_TRACKING] = roomId
        parameters[PROFESSIONAL_HASH_TRACKING] = professionalHash
        parameters[SPECIALITY_TRACKING] = speciality
        sendEventBroadcast(parameters)
    }

    fun viewProfessionalProfile(professionalHash: String, speciality: String) {
        val parameters: MutableMap<String, Any> = HashMap()
        parameters[EVENT_TYPE_TRACKING] = PROFESSIONAL_PROFILE_VIEW_TRACKING
        parameters[PROFESSIONAL_HASH_TRACKING] = professionalHash
        parameters[SPECIALITY_TRACKING] = speciality
        sendEventBroadcast(parameters)
    }

    fun messageSent(roomId: Int, professionalHash: String, speciality: String, messageType: String, messageId: String, message: String) {
        val parameters: MutableMap<String, Any> = HashMap()
        parameters[EVENT_TYPE_TRACKING] = CHAT_MESSAGE_SENT_TRACKING
        parameters[ROOM_ID_TRACKING] = roomId
        parameters[PROFESSIONAL_HASH_TRACKING] = professionalHash
        parameters[SPECIALITY_TRACKING] = speciality
        parameters[MESSAGE_TYPE_TRACKING] = messageType
        parameters[MESSAGE_ID_TRACKING] = messageId
        parameters[MESSAGE_TRACKING] = message
        sendEventBroadcast(parameters)
    }

    fun messageReceived(roomId: Int, professionalHash: String, speciality: String, messageType: String, messageId: String, message: String) {
        val parameters: MutableMap<String, Any> = HashMap()
        parameters[EVENT_TYPE_TRACKING] = CHAT_MESSAGE_RECEIVED_TRACKING
        parameters[ROOM_ID_TRACKING] = roomId
        parameters[PROFESSIONAL_HASH_TRACKING] = professionalHash
        parameters[SPECIALITY_TRACKING] = speciality
        parameters[MESSAGE_TYPE_TRACKING] = messageType
        parameters[MESSAGE_ID_TRACKING] = messageId
        parameters[MESSAGE_TRACKING] = message
        sendEventBroadcast(parameters)
    }

    private fun sendEventBroadcast(parameters: Map<String, Any>?) {
        val intent = Intent(context.getString(R.string.meetingdoctors_local_broadcast_chat_events))
        if (parameters != null) {
            val bundle = Bundle()
            for ((key, value) in parameters) {
                if (value is String) {
                    bundle.putString(key, value)
                } else if (value is Int) {
                    bundle.putInt(key, value)
                } else if (value is Long) {
                    bundle.putLong(key, value)
                } else if (value is Double) {
                    bundle.putDouble(key, value)
                }
            }
            intent.putExtras(bundle)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}
