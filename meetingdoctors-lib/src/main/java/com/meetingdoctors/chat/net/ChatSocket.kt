package com.meetingdoctors.chat.net

import android.app.Activity
import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.meetingdoctors.chat.data.Constants
import com.meetingdoctors.chat.data.Message
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.data.webservices.getChatServer
import com.meetingdoctors.chat.domain.entitesextensions.getPendingMessageCount
import com.meetingdoctors.chat.fcm.MDFirebaseMessagingService
import com.meetingdoctors.chat.helpers.StringHelper.Companion.urlEncode
import com.meetingdoctors.chat.helpers.getFileNameFromUri
import com.meetingdoctors.chat.helpers.readFileFromUri
import com.meetingdoctors.chat.presentation.entitiesextensions.arrayFromJson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject
import java.io.RandomAccessFile
import java.net.URISyntaxException
import java.util.*

/**
 * Created by Héctor Manrique on 4/14/21.
 */

class ChatSocket(activity: Activity) {

    private val activity: Activity = activity
    private var ready = false
    private var typing = false
    private val typingHandler = Handler()
    private val watchdogHandler = Handler()
    private var resumed = false
    private var lastConnectAttempt: Long = 0
    private var onConnectListener: OnConnectListener? = null
    private var onDisconnectListener: OnDisconnectListener? = null
    private var onDoctorListListener: OnDoctorListListener? = null
    private var onMessageListener: OnMessageListener? = null
    private var onTypingListener: OnTypingListener? = null
    private var onStopTypingListener: OnStopTypingListener? = null
    private var onEnterRoomListener: OnEnterRoomListener? = null
    private var onMessageListListener: OnMessageListListener? = null
    private var onMessageStatusListener: OnMessageStatusListener? = null
    private val PROFESSIONAL_LIST_EVENT = "professionalList"
    private val DOCTOR_LIST_CHANGED_EVENT = "doctorListChanged"
    private val MESSAGE_EVENT = "message"
    private val TYPING_EVENT = "typing"
    private val STOP_TYPING_EVENT = "stopTyping"
    private val ENTER_ROOM_EVENT = "enterRoom"
    private val MESSAGE_LIST_EVENT = "messageList"
    private val MESSAGE_STATUS_EVENT = "messageStatus"
    private val READY_EVENT = "ready"
    private val ROOM_ID = "roomId"
    private val DELETE_ROOM_EVENT = "deleteRoom"
    private val GET_MESSAGE_LIST_EVENT = "getMessageList"

    companion object {
        private const val TYPING_TIMER_LENGTH = 2000
    }

    init {
        watchdogHandler.postDelayed(object : Runnable {
            override fun run() {
                if (Repository.instance?.getUserData() != null
                        && Repository.instance?.getSocket() != null
                        && !Repository.instance?.getSocket()!!.connected()
                        && lastConnectAttempt + Constants.CHAT_SOCKET_WATCHDOG_FREQUENCY < Date().time) {
                    Log.i("ChatSocket", "watchdog")
                    Repository.instance!!.getSocket()!!.connect()
                }
                watchdogHandler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    fun resume() {
        if (resumed) return
        resumed = true
        var socket = Repository.instance?.getSocket()
        if (socket == null) {
            try {
                Log.i("ChatSocket",
                        "onResume connecting socket with sessionToken[${Repository.instance?.getSessionToken()}]")
                val options = IO.Options()
                options.forceNew = true
                options.reconnection = true
                options.reconnectionDelay = Constants.SOCKETIO_RECONNECTION_DELAY.toLong()
                options.reconnectionDelayMax = Constants.SOCKETIO_RECONNECTION_DELAY.toLong()
                options.reconnectionAttempts = Int.MAX_VALUE
                options.secure = true
                val transportOptions = arrayOf("websocket")
                options.transports = transportOptions
                Repository.instance?.getEnvironmentTarget()?.let {
                    socket = IO.socket(getChatServer(it) +
                            "?cushash=" + Repository.instance?.getSessionToken() +
                            "&apikey=" + urlEncode(Repository.instance?.getApiKey()),
                            options)
                }
                socket?.let {
                    Repository.instance?.setSocket(it)
                }
            } catch (e: URISyntaxException) {
                throw RuntimeException(e)
            }
        }
        socket?.on(Socket.EVENT_CONNECTING, onConnecting)
        socket?.on(Socket.EVENT_RECONNECTING, onReconnecting)
        socket?.on(Socket.EVENT_DISCONNECT, onDisconnect)
        socket?.on(READY_EVENT, onReady)
        if (onDoctorListListener != null) socket?.on(PROFESSIONAL_LIST_EVENT, onDoctorList)
        if (onDoctorListListener != null) socket?.on(DOCTOR_LIST_CHANGED_EVENT, onDoctorListChanged)
        if (onMessageListener != null) socket?.on(MESSAGE_EVENT, onMessage)
        if (onTypingListener != null) socket?.on(TYPING_EVENT, onTyping)
        if (onStopTypingListener != null) socket?.on(STOP_TYPING_EVENT, onStopTyping)
        if (onEnterRoomListener != null) socket?.on(ENTER_ROOM_EVENT, onEnterRoom)
        if (onMessageListListener != null) socket?.on(MESSAGE_LIST_EVENT, onMessageList)
        if (onMessageStatusListener != null) socket?.on(MESSAGE_STATUS_EVENT, onMessageStatus)
        socket?.on(Socket.EVENT_CONNECT) {
            Log.i("ChatSocket", "EVENT_CONNECT")
            lastConnectAttempt = Date().time
        }
        socket?.on(Socket.EVENT_CONNECT_ERROR) { Log.i("ChatSocket", "EVENT_CONNECT_ERROR") }
        socket?.on(Socket.EVENT_CONNECT_TIMEOUT) { Log.i("ChatSocket", "EVENT_CONNECT_TIMEOUT") }
        socket?.on(Socket.EVENT_RECONNECT) { Log.i("ChatSocket", "EVENT_RECONNECT") }
        socket?.on(Socket.EVENT_RECONNECT_ATTEMPT) { Log.i("ChatSocket", "EVENT_RECONNECT_ATTEMPT") }
        socket?.on(Socket.EVENT_RECONNECT_ERROR) { Log.i("ChatSocket", "EVENT_RECONNECT_ERROR") }
        socket?.on(Socket.EVENT_RECONNECT_FAILED) { Log.i("ChatSocket", "EVENT_RECONNECT_FAILED") }
        socket?.on(Socket.EVENT_ERROR) { args -> Log.i("ChatSocket", "EVENT_ERROR " + args[0]) }
        if (socket?.connected() == true) {
            if (onConnectListener != null) onConnectListener!!.onConnect()
        } else {
            socket?.connect()
        }
    }

    fun pause() {
        if (!resumed) return
        resumed = false
        val socket = Repository.instance?.getSocket()
        if (socket != null) {
            socket.off(Socket.EVENT_CONNECTING, onConnecting)
            socket.off(Socket.EVENT_RECONNECTING, onReconnecting)
            socket.off(Socket.EVENT_DISCONNECT, onDisconnect)
            socket.off(READY_EVENT, onReady)
            socket.off(PROFESSIONAL_LIST_EVENT, onDoctorList)
            socket.off(DOCTOR_LIST_CHANGED_EVENT, onDoctorListChanged)
            socket.off(MESSAGE_EVENT, onMessage)
            socket.off(TYPING_EVENT, onTyping)
            socket.off(STOP_TYPING_EVENT, onStopTyping)
            socket.off(ENTER_ROOM_EVENT, onEnterRoom)
            socket.off(MESSAGE_LIST_EVENT, onMessageList)
            socket.off(MESSAGE_STATUS_EVENT, onMessageStatus)
            //if(!ready) socket.disconnect();
        }
    }

    fun getDoctorList() {
        val socket = Repository.instance?.getSocket()
        Log.i("ChatSocket", "getDoctorList")
        if (socket != null && socket.connected()) {
            socket.emit("getProfessionalList")
        }
    }

    /********************/
    private val onConnecting = Emitter.Listener {
        Log.i("ChatSocket", "EVENT_CONNECTING")
        lastConnectAttempt = Date().time
    }

    /********************/
    private val onReconnecting = Emitter.Listener {
        Log.i("ChatSocket", "EVENT_RECONNECTING")
        lastConnectAttempt = Date().time
    }
    private val onReady = Emitter.Listener {
        if (ready) {
            Log.w("ChatSocket", "onReady discarded")
        } else {
            Log.i("ChatSocket", "onReady")
            ready = true
            if (onConnectListener != null) onConnectListener!!.onConnect()
        }
    }

    interface OnConnectListener {
        fun onConnect()
    }

    fun setOnConnectListener(onConnectListener: OnConnectListener?) {
        this.onConnectListener = onConnectListener
    }

    /********************/
    private val onDisconnect = Emitter.Listener {
        Log.i("ChatSocket", "onDisconnect")
        lastConnectAttempt = Date().time
        ready = false
        if (onDisconnectListener != null) onDisconnectListener!!.onDisconnect()
    }

    interface OnDisconnectListener {
        fun onDisconnect()
    }

    fun setOnDisconnectListener(onDisconnectListener: OnDisconnectListener?) {
        this.onDisconnectListener = onDisconnectListener
    }

    /********************/
    private val onDoctorList = Emitter.Listener { args ->
        Log.i("ChatSocket", "onDoctorList")
        try {
            val doctors = arrayFromJson(args[0].toString())
            Repository.instance?.setDoctors(doctors)
            if (onDoctorListListener != null) onDoctorListListener!!.onDoctorList()
            var notificationCount: Long = 0
            for (doctor in doctors) {
                notificationCount += doctor.getPendingMessageCount().toLong()
            }
            if (notificationCount == 0L) {
                MDFirebaseMessagingService.hidePendingMessagesNotification(activity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private var professionalListTimestamp: Long = 0
    private val onDoctorListChanged = Emitter.Listener {
        Log.i("ChatSocket", "onDoctorListChanged")
        try {
            if (professionalListTimestamp + 1000 < System.currentTimeMillis()) { // filter repeated
                professionalListTimestamp = System.currentTimeMillis()
                Repository.instance?.getSocket()?.emit("getProfessionalList")
            } else {
                Log.i("ChatSocket", "onDoctorListChanged discarded")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface OnDoctorListListener {
        fun onDoctorList()
    }

    fun setOnDoctorListListener(onDoctorListListener: OnDoctorListListener?) {
        this.onDoctorListListener = onDoctorListListener
    }

    /********************/
    private val onMessage = Emitter.Listener { args ->
        Log.i("ChatSocket", "onMessage")
        val message = args[0] as JSONObject
        try {
            if (message.has("roomId")) {
                if (onStopTypingListener != null) onStopTypingListener!!.onStopTyping(message.getInt("roomId"))
                if (onMessageListener != null) onMessageListener!!.onMessage(message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface OnMessageListener {
        fun onMessage(message: JSONObject?)
    }

    fun setOnMessageListener(onMessageListener: OnMessageListener?) {
        this.onMessageListener = onMessageListener
    }

    /********************/
    private val onTyping = Emitter.Listener { args ->
        Log.i("ChatSocket", "onTyping")
        try {
            val typingData = args[0] as JSONObject
            if (typingData.has(ROOM_ID)) {
                val roomId = typingData.getInt(ROOM_ID)
                if (onTypingListener != null) onTypingListener!!.onTyping(roomId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface OnTypingListener {
        fun onTyping(roomId: Int)
    }

    fun setOnTypingListener(onTypingListener: OnTypingListener?) {
        this.onTypingListener = onTypingListener
    }

    /********************/
    private val onStopTyping = Emitter.Listener { args ->
        Log.i("ChatSocket", "onStopTyping")
        try {
            val typingData = args[0] as JSONObject
            if (typingData.has(ROOM_ID)) {
                val roomId = typingData.getInt(ROOM_ID)
                if (onStopTypingListener != null) onStopTypingListener!!.onStopTyping(roomId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface OnStopTypingListener {
        fun onStopTyping(roomId: Int)
    }

    fun setOnStopTypingListener(onStopTypingListener: OnStopTypingListener?) {
        this.onStopTypingListener = onStopTypingListener
    }

    /********************/
    private val onEnterRoom = Emitter.Listener { args ->
        Log.i("ChatSocket", "onEnterRoom")
        try {
            ready = true
            if (onEnterRoomListener != null) onEnterRoomListener!!.onEnterRoom(args[0] as Int)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface OnEnterRoomListener {
        fun onEnterRoom(roomId: Int)
    }

    fun setOnEnterRoomListener(onEnterRoomListener: OnEnterRoomListener?) {
        this.onEnterRoomListener = onEnterRoomListener
    }

    /********************/
    private val onMessageList = Emitter.Listener { args ->
        Log.i("ChatSocket", "onMessageList")
        try {
            val messages = args[0] as JSONArray
            if (onMessageListListener != null) onMessageListListener!!.onMessageList(messages)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface OnMessageListListener {
        fun onMessageList(messages: JSONArray?)
    }

    fun setOnMessageListListener(onMessageListListener: OnMessageListListener?) {
        this.onMessageListListener = onMessageListListener
    }

    /********************/
    private val onMessageStatus = Emitter.Listener { args ->
        Log.i("ChatSocket", "onMessageStatus")
        val message = args[0] as JSONObject
        try {
            if (onMessageStatusListener != null) onMessageStatusListener!!.onMessageStatus(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface OnMessageStatusListener {
        fun onMessageStatus(message: JSONObject?)
    }

    fun setOnMessageStatus(onMessageStatusListener: OnMessageStatusListener?) {
        this.onMessageStatusListener = onMessageStatusListener
    }

    /** */
    fun enterRoom(doctorHash: String?) {
        Repository.instance?.getSocket()?.emit(ENTER_ROOM_EVENT, doctorHash)
    }

    fun clearRoom(roomId: Int) {
        Repository.instance?.getSocket()?.emit(DELETE_ROOM_EVENT, roomId)
    }

    fun typing(roomId: Int) {
        val socket = Repository.instance?.getSocket()
        if (socket != null && !typing) {
            typing = true
            socket.emit(TYPING_EVENT, roomId)
            typingHandler.removeCallbacksAndMessages(null)
            typingHandler.postDelayed(OnTypingTimeout(roomId), TYPING_TIMER_LENGTH.toLong())
        }
    }

    fun stopTyping(roomId: Int) {
        val socket = Repository.instance?.getSocket()
        if (socket != null && typing) {
            typing = false
            socket.emit(STOP_TYPING_EVENT, roomId)
        }
    }

    internal inner class OnTypingTimeout(var roomId: Int) : Runnable {
        override fun run() {
            if (!typing) return
            typing = false
            Repository.instance?.getSocket()?.emit(STOP_TYPING_EVENT, roomId)
        }

    }

    fun sendMessage(roomId: Int, message: String): String? {
        var message = message
        var messageId: String? = null
        if (ready) {
            message = message.trim { it <= ' ' }
            if (!TextUtils.isEmpty(message)) {
                stopTyping(roomId)
                messageId = UUID.randomUUID().toString()
                Repository.instance?.getSocket()?.emit(MESSAGE_EVENT, message, roomId, messageId, "string")
                Log.i("ChatSocket", "sendMessage roomId[$roomId] messageId[$messageId] string[$message]")
            }
        }
        return messageId
    }

    fun sendImage(roomId: Int, path: String?, name: String): String? {
        var messageId: String? = null
        if (ready) {
            stopTyping(roomId)
            try {
                val file = RandomAccessFile(path, "r")
                val bytes = ByteArray(file.length().toInt())
                file.read(bytes)
                val base64image = Base64.encodeToString(bytes, Base64.NO_WRAP)
                messageId = UUID.randomUUID().toString()
                Repository.instance?.getSocket()?.emit(MESSAGE_EVENT, base64image, roomId, messageId, "image", name)
                Log.i("ChatSocket", "sendImage roomId[$roomId] messageId[$messageId] image[$name]")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return messageId
    }

    fun sendFile(roomId: Int, fileUri: Uri): String? {
        var messageId: String? = null
        if (ready) {
            stopTyping(roomId)
            try {
                val bytes = readFileFromUri(activity, fileUri)
                val base64image = Base64.encodeToString(bytes, Base64.NO_WRAP)
                messageId = UUID.randomUUID().toString()
                Repository.instance?.getSocket()?.emit(MESSAGE_EVENT, base64image, roomId, messageId, "file", getFileNameFromUri(activity, fileUri))
                Log.i("ChatSocket", "sendFile roomId[$roomId] messageId[$messageId] file[$fileUri]")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return messageId
    }

    fun getMessageList(roomId: Int, numOfItems: Int, pivotMessageId: String?, mode: Int) {
        if (ready) {
            Log.i("ChatSocket", "getMessageList")
            Repository.instance?.getSocket()?.emit(GET_MESSAGE_LIST_EVENT, roomId, numOfItems, pivotMessageId, mode)
        }
    }

    fun getMessageList(roomId: Int, numOfItems: Int) {
        if (ready) {
            Log.i("ChatSocket", "getMessageList")
            Repository.instance?.getSocket()?.emit(GET_MESSAGE_LIST_EVENT, roomId, numOfItems)
        }
    }

    fun sendMessageStatus(roomId: Int, message: Message) {
        if (ready) {
            Log.i("ChatSocket", "sendMessageStatus")
            Repository.instance?.getSocket()?.emit(MESSAGE_STATUS_EVENT, message.id, roomId, message.status)
        }
    }

    fun isReady(): Boolean {
        return ready
    }
}
