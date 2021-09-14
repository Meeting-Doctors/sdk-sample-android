package com.meetingdoctors.chat.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meetingdoctors.chat.MeetingDoctorsClient
import com.meetingdoctors.chat.MeetingDoctorsClient.Companion.videoCallRequestListener
import com.meetingdoctors.chat.MeetingDoctorsClient.OnVideoCall1to1RequestDone
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.TitleBarBaseActivity
import com.meetingdoctors.chat.adapters.MessageAdapter
import com.meetingdoctors.chat.data.Cache.Companion.getMessages
import com.meetingdoctors.chat.data.Cache.Companion.putMessages
import com.meetingdoctors.chat.data.Constants
import com.meetingdoctors.chat.data.Message
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.data.Speciality.Companion.name
import com.meetingdoctors.chat.domain.DOCTOR_ROLE_DOCTOR
import com.meetingdoctors.chat.domain.DOCTOR_ROLE_FREEMIUM_DOCTOR
import com.meetingdoctors.chat.domain.DOCTOR_STATUS_OFFLINE
import com.meetingdoctors.chat.domain.DOCTOR_STATUS_ONLINE
import com.meetingdoctors.chat.domain.entitesextensions.getPendingMessageCount
import com.meetingdoctors.chat.domain.entitesextensions.getRoomId
import com.meetingdoctors.chat.domain.entitesextensions.isVideoCallOneToOneEnabled
import com.meetingdoctors.chat.domain.entitesextensions.setRoomId
import com.meetingdoctors.chat.domain.entities.Doctor
import com.meetingdoctors.chat.fcm.MDFirebaseMessagingService.Companion.hidePendingMessagesNotification
import com.meetingdoctors.chat.fcm.MDFirebaseMessagingService.Companion.hidePendingMessagesNotificationByRoom
import com.meetingdoctors.chat.fcm.MDFirebaseMessagingService.Companion.showPendingMessagesNotification
import com.meetingdoctors.chat.helpers.*
import com.meetingdoctors.chat.helpers.BitmapHelper.Companion.getImageSize
import com.meetingdoctors.chat.helpers.BitmapHelper.Companion.rotateAndResizeImage
import com.meetingdoctors.chat.helpers.BitmapHelper.Companion.saveBitmapToTempFile
import com.meetingdoctors.chat.helpers.SystemHelper.Companion.isPermissionDeclared
import com.meetingdoctors.chat.net.ChatSocket
import com.meetingdoctors.chat.presentation.entitiesextensions.isSameDay
import com.meetingdoctors.chat.views.ChatRecyclerViewScrollListener
import com.meetingdoctors.chat.views.ChatTitleBar
import kotlinx.android.synthetic.main.mediquo_activity_chat.*
import kotlinx.android.synthetic.main.mediquo_layout_chat_title_bar.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*


/**
 * Created by Héctor Manrique on 4/19/21.
 */

class ChatActivity : TitleBarBaseActivity(), OnVideoCall1to1RequestDone {

    companion object {

        const val CHAT_DOCTOR_USER_HASH_KEY = "doctorUserHash"
        const val CHAT_OUTGOING_MESSAGE_KEY = "outgoingMessage"
        const val CHAT_INCOMING_MESSAGE_KEY = "incomingMessage"

        @JvmStatic
        fun getIntent(context: Context,
                      doctorUserHash: String,
                      outgoingMessage: String?,
                      incomingMessage: String?): Intent {

            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(CHAT_DOCTOR_USER_HASH_KEY, doctorUserHash)
            if (outgoingMessage != null) {
                intent.putExtra(CHAT_OUTGOING_MESSAGE_KEY, outgoingMessage)
            }
            if (incomingMessage != null) {
                intent.putExtra(CHAT_INCOMING_MESSAGE_KEY, incomingMessage)
            }

            return intent
        }

    }

    private val PICK_IMAGE_REQUEST_CODE = 1
    private lateinit var messageAdapter: MessageAdapter
    private val filePickerHandler = Handler()
    private lateinit var doctor: Doctor
    private var outgoingMessage: String? = null
    private var incomingMessage: String? = null
    val messages: MutableList<Message> = ArrayList()
    private var photo: File? = null
    private var pushBroadcastReceiver: PushBroadcastReceiver? = null
    private var offlineAlertShown = false
    var chatSocket: ChatSocket? = null
    private var popup: PopupMenu? = null

    private fun checkEssentialInfo() {
        if (MeetingDoctorsClient.instance == null) {
            finish()
            return
        }
        val userHash = intent.getStringExtra("doctorUserHash")
        if (userHash == null) {
            finish()
            return
        }
        val doctorByHash = Repository.instance?.getDoctorByUserHash(userHash)
        if (doctorByHash == null) {
            finish()
            return
        }
        doctor = doctorByHash
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("ChatActivity", "onCreate")
        super.onCreate(savedInstanceState)

        checkEssentialInfo()

        outgoingMessage = intent.getStringExtra("outgoingMessage")
        incomingMessage = intent.getStringExtra("incomingMessage")

        setTitleBar(ChatTitleBar(this, doctor.name, doctor.title))
        setContentView(R.layout.mediquo_activity_chat)

        //popup menu
        popup = PopupMenu(this@ChatActivity, menu_chat)
        popup?.menuInflater?.inflate(R.menu.mediquo_menu_chat, popup?.menu)

        // hide room notification
        if (doctor.getRoomId() != null) {
            hidePendingMessagesNotificationByRoom(this, doctor.getRoomId()!!)
        }
        // hide global notification
        if (doctor.getPendingMessageCount() > 0
                && doctor.getPendingMessageCount().toLong() == Repository.instance?.getPendingMessagesCount()) {
            hidePendingMessagesNotification(this)
        }
        if (doctor.avatar.isNotEmpty()) {
            Glide.with(this).load(doctor.avatar).apply(RequestOptions.circleCropTransform()).into(photo_doctor)
            photo_doctor?.visibility = View.VISIBLE
        } else {
            photo_doctor?.visibility = View.INVISIBLE
        }
        profile_button?.setOnClickListener { launchDoctorProfile(this@ChatActivity, doctor.hash) }
        profile_button_2?.setOnClickListener { launchDoctorProfile(this@ChatActivity, doctor.hash) }
        attachment_button?.setOnClickListener { pickFile() }
        menu_chat?.setOnClickListener { showMenu() }
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        messageAdapter = MessageAdapter(this, messages)
        chatRecyclerView?.apply {
            layoutManager = linearLayoutManager
            adapter = messageAdapter
            addOnScrollListener(OnScrollListener(linearLayoutManager))
        }
        message_input?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (doctor.getRoomId() == null) return
                chatSocket?.typing(doctor.getRoomId()!!)
            }

            override fun afterTextChanged(s: Editable) {}
        })
        val sendButton = findViewById<TextView>(R.id.send_button)
        sendButton.setOnClickListener {
            val message = message_input?.text.toString()
            sendMessage(message)
            if (Repository.instance!!.getMessageFromDoctorCounter() >= 4 && message.toLowerCase().contains("gracias")) {
                if (MeetingDoctorsClient.instance!!.ratingRequestListener != null) {
                    Repository.instance!!.setMessageFromDoctorCounter(0)
                    MeetingDoctorsClient.instance!!.ratingRequestListener!!.onPassiveRatingRequest()
                }
            }
        }

        initSocket()
    }

    private fun initSocket() {
        pushBroadcastReceiver = PushBroadcastReceiver()
        chatSocket = ChatSocket(this)
        chatSocket?.setOnConnectListener(OnConnectListener())
        chatSocket?.setOnDisconnectListener(OnDisconnectListener())
        chatSocket?.setOnDoctorListListener(OnDoctorListListener())
        chatSocket?.setOnMessageListener(OnMessageListener())
        chatSocket?.setOnEnterRoomListener(OnEnterRoomListener())
        chatSocket?.setOnMessageStatus(OnMessageStatusListener())
        chatSocket?.setOnMessageListListener(OnMessageListListener())
        chatSocket?.setOnTypingListener(OnTypingListener())
        chatSocket?.setOnStopTypingListener(OnStopTypingListener())
    }

    public override fun onDestroy() {
        Log.i("ChatActivity", "onDestroy")
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        // load messages from cache only when isComputingLayout == false
        chatRecyclerView?.post(object : Runnable {
            override fun run() {
                // it's magic
                if (chatRecyclerView?.isComputingLayout == true) {
                    chatRecyclerView?.postDelayed(this, 10)
                    return
                }
                // draw messages from cache
                if (doctor.getRoomId() != null) {
                    syncMessagesFromCache()
                }
            }
        })
        doctor.getRoomId()?.let {
            Repository.instance?.getTracking()?.viewChat(
                    it,
                    doctor.hash,
                    name(doctor.speciality?.id)
            )
        }
    }

    override fun onReady() {
        Log.i("ChatActivity", "onReady()")
        super.onReady()
        chatSocket?.resume()
        setStatus(Repository.instance?.getSocket()?.connected() ?: false)
        attachment_button?.setImageResource(
                if (Repository.instance?.getSocket()?.connected() == true) {
                    R.drawable.mediquo_attach_icon
                } else {
                    R.drawable.mediquo_attach_icon_disabled
                }
        )
        pushBroadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this)
                    .registerReceiver(it,
                            IntentFilter(getString(R.string.meetingdoctors_local_broadcast_gcm_message_received)))
        }

    }

    override fun onPause() {
        Log.i("ChatActivity", "onPause")
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver!!)
        filePickerHandler.removeCallbacksAndMessages(null)
        chatSocket?.pause()

        // store messages in cache
        if (doctor.getRoomId() != null) {
            val cachedMessages: MutableList<Message> = ArrayList()
            cachedMessages.addAll(messages)
            // remove unsent messages
            for (i in cachedMessages.indices.reversed()) {
                if (cachedMessages[i].status == null || cachedMessages[i].status!! < 1) {
                    cachedMessages.removeAt(i)
                }
            }
            doctor.room?.id?.let {
                if (cachedMessages.size > Constants.CHAT_PAGE_SIZE) {
                    putMessages(this, it, cachedMessages.subList(cachedMessages.size
                            - Constants.CHAT_PAGE_SIZE, cachedMessages.size))
                } else {
                    putMessages(this, it, cachedMessages)
                }
            }

        }
    }

    private inner class OnConnectListener : ChatSocket.OnConnectListener {
        override fun onConnect() {
            runOnUiThread {
                try {
                    chatSocket!!.enterRoom(doctor.hash)
                    setStatus(true)
                    send_button?.setTextColor(ContextCompat.getColor(this@ChatActivity,
                            R.color.meetingdoctors_black))
                    attachment_button?.setImageResource(R.drawable.mediquo_attach_icon)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private inner class OnDisconnectListener : ChatSocket.OnDisconnectListener {
        override fun onDisconnect() {
            runOnUiThread {
                setStatus(false)
                send_button?.setTextColor(ContextCompat.getColor(this@ChatActivity,
                        R.color.meetingdoctors_dark_gray))
                attachment_button?.setImageResource(R.drawable.mediquo_attach_icon_disabled)
            }
        }
    }

    private inner class OnDoctorListListener : ChatSocket.OnDoctorListListener {
        override fun onDoctorList() {
            runOnUiThread { setStatus(true) }
        }
    }

    private inner class OnEnterRoomListener : ChatSocket.OnEnterRoomListener {
        override fun onEnterRoom(roomId: Int) {
            runOnUiThread {
                Log.i("ChatActivity", "onEnterRoom")
                try {
                    if (doctor.getRoomId() != null && messages.size > 0) {
                        chatSocket?.getMessageList(doctor.getRoomId()!!, Constants.CHAT_PAGE_SIZE,
                                messages[0].id, 1 /*older than*/)
                    } else {
                        doctor.setRoomId(roomId)
                        chatSocket?.getMessageList(doctor.getRoomId()!!, Constants.CHAT_PAGE_SIZE)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setStatus(socketConnected: Boolean) {
        var doctorStatusResource = R.drawable.mediquo_circle_shadow_gray
        if (socketConnected) {
            val doctorByHash = Repository.instance?.getDoctorByUserHash(doctor.hash)
            if (doctorByHash == null) {
                finish()
                return
            }
            doctor.status = doctorByHash.status
            doctorStatusResource = when (doctor.status) {
                DOCTOR_STATUS_OFFLINE -> R.drawable.mediquo_circle_shadow_red
                DOCTOR_STATUS_ONLINE -> R.drawable.mediquo_circle_shadow_green
                else -> R.drawable.mediquo_circle_shadow_gray
            }
        }
        doctor_status?.setBackgroundResource(doctorStatusResource)

    }

    private fun pickFile() {
        if (isPermissionDeclared(this, Manifest.permission.CAMERA)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.CAMERA")
                ActivityCompat.requestPermissions(this, permissions, 1)
            } else {
                pickFileActually()
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf("android.permission.READ_EXTERNAL_STORAGE")
                ActivityCompat.requestPermissions(this, permissions, 1)
            } else {
                pickFileActually()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickFileActually()
        }
    }

    private fun pickFileActually() {
        try {
            val intentList: MutableList<Intent> = ArrayList()
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val dir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.absolutePath)
            photo = File(dir, "/MD_" + Date().time + ".jpg")
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo))
            intentList.add(cameraIntent)
            val mimeTypes = arrayOf("application/msword",
                    "appliccation/vnd.ms-excel",
                    "text/plain",
                    "text/html",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.ms-powerpoint",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "application/vnd.ms-word.document.macroEnabled.12",
                    "application/vnd.ms-word.template.macroEnabled.12",
                    "application/pdf",
                    "video/mpeg",
                    "video/mp4",
                    "video/3gpp",
                    "video/quicktime")
            val fileIntent = Intent(Intent.ACTION_GET_CONTENT)
            fileIntent.type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document |" +
                    "application/pdf|" + "application/msword|" +
                    "appliccation/vnd.ms-excel|" +
                    "text/plain|" +
                    "text/html|" +
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document|" +
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.template|" +
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet|" +
                    "application/vnd.ms-powerpoint|" +
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation|" +
                    "application/vnd.ms-word.document.macroEnabled.12|" +
                    "application/vnd.ms-word.template.macroEnabled.12|" +
                    "video/mpeg|" +
                    "video/mp4|" +
                    "video/3gpp" +
                    "video|quicktime"
            fileIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            fileIntent.addCategory(Intent.CATEGORY_OPENABLE)
            intentList.add(fileIntent)
            val chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.meetingdoctors_chat_pick_file_app_title))
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toTypedArray())
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST_CODE)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("onActivityResult", "File picked!")
        GlobalScope.launch { // launch a new coroutine in background and continue
            when (resultCode) {
                Activity.RESULT_OK -> processPickFileResult(Activity.RESULT_OK, requestCode, data)
                Activity.RESULT_CANCELED -> processPickFileResult(Activity.RESULT_CANCELED, requestCode, data)
                else -> Log.i("OnActivityResult", "ResultCode Undefined!")
            }
        }
    }

    private fun processPickFileResult(resultCode: Int, requestCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> if (requestCode == PICK_IMAGE_REQUEST_CODE) {
                val pickedFileUri = data?.data
                filePickerHandler.postDelayed(object : Runnable {
                    override fun run() {
                        if (chatSocket?.isReady() == true) {
                            Log.i("onActivityResult", "Sending picked file")
                            if (pickedFileUri != null) {
                                if (this@ChatActivity.isImageFile(pickedFileUri)) { // image from gallery
                                    val fileHelper = NewFileHelper()
                                    val pickedFilePath = fileHelper.getRealPathFromURI(this@ChatActivity, pickedFileUri)
                                    if (pickedFilePath != null) {
                                        GlobalScope.launch {
                                            sendImage(pickedFilePath, pickedFileUri, false)
                                        }
                                    }
                                } else { // file from file manager
                                    GlobalScope.launch {
                                        sendFile(pickedFileUri)
                                    }
                                }
                            } else { // photo from camera
                                photo?.absolutePath?.let {
                                    GlobalScope.launch {
                                        sendImage(it, Uri.fromFile(File(it)), true)
                                    }
                                }
                            }
                        } else {
                            Log.i("onActivityResult", "Waiting for connection to send picked file...")
                            filePickerHandler.postDelayed(this, 500)
                        }
                    }
                }, 500)
            }
            Activity.RESULT_CANCELED -> {
                runOnUiThread {
                    Toast.makeText(this, getText(R.string.meetingdoctors_chat_attachment_canceled), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun sendMessage(message: String) {
        var message = message
        message = message.trim { it <= ' ' }
        if (message.isEmpty()) {
            message_input?.requestFocus()
            return
        }
        if (chatSocket?.isReady() != true) {
            Toast.makeText(applicationContext, getString(R.string.meetingdoctors_not_connected), Toast.LENGTH_SHORT).show()
            return
        }
        if (doctor.getRoomId() == null) {
            return
        }
        message_input?.setText("")
        val messageId = chatSocket?.sendMessage(doctor.getRoomId()!!, message)
        addMessageString(Repository.instance?.getUserHash(), messageId, Date().time, 0, message)
        reportMessageSent("string", messageId, message)
    }

    private fun sendMessageToExternalRoom(message: String, roomId: String?, hash: String?) {
        var message = message
        message = message.trim { it <= ' ' }
        if (chatSocket?.isReady() == false) {
            Toast.makeText(applicationContext, getString(R.string.meetingdoctors_not_connected), Toast.LENGTH_SHORT).show()
            return
        }
        roomId?.let {
            val messageId = chatSocket?.sendMessage(roomId.toInt(), message)
            if (hash != null && hash.equals(doctor.hash, ignoreCase = true)) {
                addMessageString(Repository.instance?.getUserHash(), messageId, Date().time, 0, message)
            }
            reportMessageSent("string", messageId, message)
        }
    }

    private suspend fun sendImage(imagePath: String?, uri: Uri, isPictureTakenFromCamera: Boolean) {
        if (doctor.getRoomId() == null || uri.path == null) return
        try {
            val absolutePath = this.compressImageFile(uri.path!!, false, uri)
            val rotatedAndResizedImageFile = saveBitmapToTempFile(this@ChatActivity,
                    rotateAndResizeImage(if (isPictureTakenFromCamera) imagePath!! else absolutePath,
                            2000 * 2000.toLong(),
                            isPictureTakenFromCamera))
            val fileName = File(absolutePath).name
            val messageId = chatSocket?.sendImage(doctor.getRoomId()!!,
                    rotatedAndResizedImageFile.absolutePath, fileName)
            val size = getImageSize(rotatedAndResizedImageFile.absolutePath)
            addMessageImage(Repository.instance!!.getUserHash(), messageId,
                    Date().time, 0, imagePath, imagePath, size.x.toLong(), size.y.toLong())
            rotatedAndResizedImageFile.delete()
            reportMessageSent("image", messageId, fileName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun sendFile(fileUri: Uri) {
        if (doctor.getRoomId() == null) return
        try {
            val bytes = readFileFromUri(this, fileUri)
            val messageId = chatSocket?.sendFile(doctor.getRoomId()!!, fileUri)
            val fileName = getFileNameFromUri(this, fileUri)
            if (this.isImageFile(fileUri)) {
                val pickedFilePath = getRealPathFromURI(this@ChatActivity, fileUri)
                sendImage(pickedFilePath, fileUri, false)
            } else {
                addMessageFile(Repository.instance!!.getUserHash(), messageId, Date().time, 0, fileName, bytes!!.size.toLong())
            }
            reportMessageSent("file", messageId, fileName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scrollToBottom() {
        Handler().postDelayed({ chatRecyclerView?.scrollToPosition(messageAdapter.itemCount - 1) }, 100)
    }

    private inner class OnMessageListener : ChatSocket.OnMessageListener {
        override fun onMessage(message: JSONObject?) {
            runOnUiThread {
                try {
                    Log.i("ChatActivity", "onMessage")
                    removeTyping()
                    addMessage(message, true)
                    if (doctor.app_role?.id == DOCTOR_ROLE_DOCTOR ||
                            doctor.app_role?.id == DOCTOR_ROLE_FREEMIUM_DOCTOR) {
                        Repository.instance?.getMessageFromDoctorCounter()?.let {
                            Repository.instance?.setMessageFromDoctorCounter(
                                    it + 1
                            )

                            if (it >= 16) {
                                MeetingDoctorsClient.instance?.ratingRequestListener?.let {
                                    Repository.instance?.setMessageFromDoctorCounter(0)
                                    MeetingDoctorsClient.instance?.ratingRequestListener?.onPassiveRatingRequest()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private inner class OnTypingListener : ChatSocket.OnTypingListener {
        override fun onTyping(roomId: Int) {
            runOnUiThread {
                try {
                    Log.i("ChatActivity", "onTyping")
                    if (roomId == doctor.getRoomId()) {
                        addTyping()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private inner class OnStopTypingListener : ChatSocket.OnStopTypingListener {
        override fun onStopTyping(roomId: Int) {
            runOnUiThread {
                try {
                    Log.i("ChatActivity", "OnStopTypingListener")
                    if (roomId == doctor.getRoomId()) {
                        removeTyping()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    //TODO check this
    private inner class OnMessageListListener : ChatSocket.OnMessageListListener {
        override fun onMessageList(messages: JSONArray?) {
            runOnUiThread {
                try {
                    Log.i("ChatActivity", "onMessageList() " + messages!!.length() + " received")
                    syncMessages(messages)

                    if (outgoingMessage != null) {
                        sendMessage(outgoingMessage!!)
                        outgoingMessage = null
                    }
                    if (incomingMessage != null) {
                        receiveMessage(this@ChatActivity, doctor.hash, incomingMessage)
                        incomingMessage = null
                    }
                    if (!offlineAlertShown) {
                        offlineAlertShown = true
                        if (doctor.status == DOCTOR_STATUS_OFFLINE) {
                            drawMessage(Message.Builder()
                                    .type(Message.TYPE_ALERT)
                                    .string(getString(R.string.meetingdoctors_chat_offline_alert))
                                    .time(System.currentTimeMillis())
                                    .build())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private inner class OnMessageStatusListener : ChatSocket.OnMessageStatusListener {
        override fun onMessageStatus(message: JSONObject?) {
            runOnUiThread {
                Log.i("ChatActivity", "OnMessageStatusListener")
                try {
                    val messageId = message!!.getString("messageId")
                    val status = message.getInt("status")
                    val time = if (message.has("time")) message.getLong("time") else null
                    val thumbUrl = if (message.has("thumbUrl")) message.getString("thumbUrl") else null
                    val imageUrl = if (message.has("imageUrl")) message.getString("imageUrl") else null
                    val imageWidth = if (message.has("imageWidth")) message.getLong("imageWidth") else null
                    val imageHeight = if (message.has("imageHeight")) message.getLong("imageHeight") else null
                    val fileUrl = if (message.has("fileUrl")) message.getString("fileUrl") else null
                    val fileName = if (message.has("fileName")) message.getString("fileName") else null
                    val fileSize = if (message.has("fileSize")) message.getLong("fileSize") else null
                    Log.i("ChatActivity", "onMessageStatus messageId[$messageId] status[$status] time[$time] thumbUrl[$thumbUrl] imageUrl[$imageUrl] imageWidth[$imageWidth] imageHeight[$imageHeight] fileSize[$fileSize] fileUrl[$fileUrl] fileName[$fileName]")
                    updateMessage(messageId, status, time, thumbUrl, imageUrl, imageWidth, imageHeight, fileUrl, fileName, fileSize)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun addMessage(json: JSONObject?, notify: Boolean) {
        try {
            removeTyping()

            json?.let {
                // check roomId
                val roomId = (if (json.has("roomId")) json.getInt("roomId") else null) ?: return
                if (roomId != doctor.getRoomId()) return
                when (json.getString("type")) {
                    "string", "image", "file" -> {
                    }
                    else -> return
                }
                val mine = doctor.hash != json.getString("fromUserHash")
                val message = Message.Builder(json, mine).build()
                addMessage(message, notify)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addMessage(message: Message, notify: Boolean) {
        try {
            removeTyping()
            when (message.type) {
                Message.TYPE_STRING_THEIR, Message.TYPE_IMAGE_THEIR, Message.TYPE_FILE_THEIR ->
                    message.status?.let {
                        if (it < 3) {
                            message.status = 3
                            doctor.getRoomId()?.let {
                                chatSocket?.sendMessageStatus(it, message)
                            }
                            when (message.type) {
                                Message.TYPE_STRING_THEIR -> reportMessageReceived("string", message.id, message.string)
                                Message.TYPE_IMAGE_THEIR -> reportMessageReceived("image", message.id, message.fileName)
                                Message.TYPE_FILE_THEIR -> reportMessageReceived("file", message.id, message.fileName)
                            }
                        }
                    }
            }
            drawMessage(message, notify)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addTyping() {
        setSubtitle(getString(R.string.meetingdoctors_chat_writing))
    }

    private fun removeTyping() {
        setSubtitle(doctor.title)
    }

    private fun updateMessage(messageId: String, status: Int, time: Long?, thumbUrl: String?, imageUrl: String?,
                              imageWidth: Long?, imageHeight: Long?, fileUrl: String?, fileName: String?, fileSize: Long?) {
        synchronized(messages) {
            for (i in messages.indices.reversed()) {
                if (messages[i].id != null && messages[i].id == messageId) {
                    messages[i].status = status
                    if (time != null) messages[i].time = time
                    if (thumbUrl != null) messages[i].thumbUrl = thumbUrl
                    if (imageUrl != null) messages[i].imageUrl = imageUrl
                    if (imageWidth != null) messages[i].imageWidth = imageWidth
                    if (imageHeight != null) messages[i].imageHeight = imageHeight
                    if (fileUrl != null) messages[i].fileUrl = fileUrl
                    if (fileName != null) messages[i].fileName = fileName
                    if (fileSize != null) messages[i].fileSize = fileSize
                    messageAdapter.notifyItemChanged(i)
                    break
                }
            }
        }
    }

    private fun addMessageString(fromUserHash: String?, messageId: String?, time: Long, status: Int, string: String) {
        val message = Message.Builder()
                .type(if (doctor.hash == fromUserHash) Message.TYPE_STRING_THEIR else Message.TYPE_STRING_MINE)
                .id(messageId)
                .time(time)
                .status(status)
                .string(string)
                .build()
        drawMessage(message)
    }

    private fun addMessageImage(fromUserHash: String?, messageId: String?, time: Long, status: Int, imageUrl: String?, thumbUrl: String?, imageWidth: Long, imageHeight: Long) {
        val message = Message.Builder()
                .type(if (doctor.hash == fromUserHash) Message.TYPE_IMAGE_THEIR else Message.TYPE_IMAGE_MINE)
                .id(messageId)
                .time(time)
                .status(status)
                .imageUrl(imageUrl)
                .thumbUrl(thumbUrl)
                .imageWidth(imageWidth)
                .imageHeight(imageHeight)
                .build()
        drawMessage(message)
    }

    private fun addMessageFile(fromUserHash: String?, messageId: String?, time: Long, status: Int, fileName: String?, fileSize: Long) {
        val message = Message.Builder()
                .type(if (doctor.hash == fromUserHash) Message.TYPE_FILE_THEIR else Message.TYPE_FILE_MINE)
                .id(messageId)
                .time(time)
                .status(status)
                .fileName(fileName)
                .fileSize(fileSize)
                .build()
        drawMessage(message)
    }

    private fun drawMessage(message: Message, notify: Boolean = true) {
        synchronized(messages) {

            // search id
            for (i in messages.indices) {
                if (messages[i].id != null && messages[i].id == message.id) {
                    if (messages[i].status !== message.status) {
                        messages[i].status = message.status
                        if (notify) messageAdapter.notifyItemChanged(i)
                    }
                    return
                }
            }
            // search position
            var position = messages.size
            messages.forEachIndexed { index, message ->
                if (message.time != null
                        && message.time != null
                        && message.time!! > message.time!!) {
                    position = index
                    return@forEachIndexed
                }
            }

            // add day message if necessary
            if (position > 0) {
                val previousMessage = messages[position - 1]
                if (previousMessage.type != Message.TYPE_DATE && previousMessage.time != null && message.time != null) {
                    if (!previousMessage.time!!.isSameDay(message.time!!)) {
                        Log.d("ChatActivity", "drawMessage() date added")
                        val time = message.time!! - message.time!! % (24 * 60 * 60 * 1000) // 00:00:00 ;)
                        messages.add(position, Message.Builder().type(Message.TYPE_DATE).time(time).build())
                        if (notify) messageAdapter.notifyItemInserted(position)
                        position++
                    }
                }
            } else {
                val time = message.time!! - message.time!! % (24 * 60 * 60 * 1000) // 00:00:00 ;)
                Log.d("ChatActivity", "drawMessage() date added")
                messages.add(position, Message.Builder().type(Message.TYPE_DATE).time(time).build())
                if (notify) messageAdapter.notifyItemInserted(position)
                position++
            }
            // remove day message if necessary
            if (position < messages.size - 1) { // not last
                if (messages[position + 1].type == Message.TYPE_DATE) { // next is date
                    val nextDay = Date(messages[position + 1].time!!).day
                    val day = Date(message.time!!).day
                    if (nextDay == day) { // same day
                        Log.d("ChatActivity", "drawMessage() date removed")
                        messages.removeAt(position)
                        if (notify) messageAdapter.notifyItemRemoved(position)
                    }
                }
            }
            // insert message
            if (position == messages.size) {
                Log.d("ChatActivity", "drawMessage() message added")
                messages.add(message)
                if (notify) messageAdapter.notifyItemInserted(position)
                scrollToBottom()
            } else {
                Log.d("ChatActivity", "drawMessage() message inserted")
                messages.add(position, message)
                if (notify) messageAdapter.notifyItemInserted(position)
            }
        }
    }

    private inner class PushBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            var roomId: Int? = null
            try {
                roomId = intent.extras?.getInt("roomId")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (doctor.getRoomId() != null && roomId != null && doctor.getRoomId() == roomId) {
                Log.i("ChatActivity", "PushBroadcastReceiver.onReceive() same room")
            } else {
                Log.i("ChatActivity", "PushBroadcastReceiver.onReceive() different room")
                showPendingMessagesNotification(this@ChatActivity, roomId, null, null)
            }
        }
    }

    //TODO check this
    private fun syncMessages(messages: JSONArray?) {
        messages?.let {
            for (i in 0 until messages.length()) {
                try {
                    val message = messages.getJSONObject(i)
                    addMessage(message, false)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }

        messageAdapter.notifyDataSetChanged()
    }

    private fun syncMessages(messages: List<Message>) {
        for (message in messages) {
            addMessage(message, false)
        }
        messageAdapter.notifyDataSetChanged()
    }

    private inner class OnScrollListener(layoutManager: LinearLayoutManager?) : ChatRecyclerViewScrollListener(layoutManager!!, Constants.CHAT_PAGE_SIZE) {
        override fun onLoadOlderMessages() {
            Log.i("ChatActivity", "OnScrollListener.onLoadOlderMessages()")
            for (i in messages.indices) {
                val messageId = messages[i].id
                val roomId = doctor.getRoomId()
                if (messageId != null && roomId != null) {
                    chatSocket?.getMessageList(roomId,
                            Constants.CHAT_PAGE_SIZE,
                            messageId,
                            1 /*older than*/)
                    break
                }
            }
        }
    }

    //TODO check this
    private fun syncMessagesFromCache() {
        doctor.getRoomId()?.let {
            val cachedMessages = getMessages(this@ChatActivity, it)
            if (cachedMessages != null) {
                syncMessages(cachedMessages)
            }
        }

    }

    private fun showMenu() {
        val requestVideoCallItem = popup?.menu?.findItem(R.id.request_videocall)
        val isVideoCallOneToOneEnabled = (Repository.instance?.getUserData()?.isVideoCallOneToOneEnabled() == true
                && doctor.is_vc_available)
        if (requestVideoCallItem != null && !isVideoCallOneToOneEnabled) {
            requestVideoCallItem.isVisible = false
        }
        val hasAssignedVideoCall = videoCallRequestListener != null &&
                videoCallRequestListener!!.hasProfessionalAssignedVideoCall(doctor.hash)
        if (hasAssignedVideoCall) {
            requestVideoCallItem?.title = getString(R.string.meetingdoctors_chat_cancel_videocall)
        } else requestVideoCallItem?.title = getString(R.string.meetingdoctors_chat_request_videocall)
        popup?.setOnMenuItemClickListener { item: MenuItem ->
            val itemId = item.itemId
            if (itemId == R.id.delete_chat) {
                showDeleteMessageDialog()
            } else if (itemId == R.id.request_videocall) {
                val optionTitle = popup?.menu?.findItem(R.id.request_videocall)?.title.toString()
                if (optionTitle.equals(getString(R.string.meetingdoctors_chat_request_videocall), ignoreCase = true)) {
                    if (MeetingDoctorsClient.instance != null) {
                        videoCallRequestListener!!.perform1to1VideoCall(doctor.hash,
                                doctor.name,
                                this@ChatActivity,
                                this)
                    }
                } else if (optionTitle.equals(getString(R.string.meetingdoctors_chat_cancel_videocall), ignoreCase = true)) {
                    MeetingDoctorsClient.instance?.let {
                        videoCallRequestListener?.performCancelVideoCall(this@ChatActivity,
                                getString(R.string.meetingdoctors_chat_cancel_videocall),
                                this,
                                doctor.name)
                    }
                }
            }
            true
        }
        popup?.show()
    }

    override fun performRequestDoneAction(roomId: String?) {
        val message = getString(R.string.meetingdoctors_chat_send_vc_automessage)
        sendMessage(message)
        val requestVideoCallItem = popup?.menu?.findItem(R.id.request_videocall)
        requestVideoCallItem?.title = getString(R.string.meetingdoctors_chat_cancel_videocall)
    }


    override fun performSendCancelledCallMessage(roomId: String?, hash: String?) {
        val message = getString(R.string.meetingdoctors_chat_cancelled_vc_automessage)
        sendMessageToExternalRoom(message, roomId, hash)
    }

    override fun performRequestCancelledAction() {
        val requestVideoCallItem = popup?.menu?.findItem(R.id.request_videocall)
        requestVideoCallItem?.title = getString(R.string.meetingdoctors_chat_request_videocall)
    }

    private fun showDeleteMessageDialog() {
        val builder = AlertDialog.Builder(this@ChatActivity)
        builder.setTitle(R.string.meetingdoctors_chat_clear_dialog_title)
        builder.setMessage(R.string.meetingdoctors_chat_clear_dialog_body)
        builder.setPositiveButton(R.string.meetingdoctors_dialog_accept) { dialog, which ->
            chatSocket?.clearRoom(doctor.getRoomId()!!)
            messageAdapter.clearMessages()
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.meetingdoctors_dialog_cancel) { dialog, which -> dialog.dismiss() }
        val diag = builder.create()
        diag.show()
    }

    private fun reportMessageSent(messageType: String, messageId: String?, message: String?) {
        doctor.getRoomId()?.let {
            if (messageId != null && message != null) {
                Repository.instance?.getTracking()?.messageSent(
                        it,
                        doctor.hash,
                        name(doctor.speciality?.id),
                        messageType,
                        messageId,
                        message
                )
            }
        }
    }

    private fun reportMessageReceived(messageType: String, messageId: String?, message: String?) {
        doctor.getRoomId()?.let {
            if (messageId != null && message != null) {
                Repository.instance?.getTracking()?.messageReceived(
                        it,
                        doctor.hash,
                        name(doctor.speciality!!.id),
                        messageType,
                        messageId,
                        message
                )
            }
        }
    }
}
