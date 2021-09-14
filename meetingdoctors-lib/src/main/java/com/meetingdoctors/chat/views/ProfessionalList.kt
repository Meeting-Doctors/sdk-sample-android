package com.meetingdoctors.chat.views

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.Keep
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.meetingdoctors.chat.MeetingDoctorsClient
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.BaseActivity.Companion.launchChat
import com.meetingdoctors.chat.adapters.DoctorChatAdapter
import com.meetingdoctors.chat.data.Constants
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.data.Speciality.Companion.flag
import com.meetingdoctors.chat.data.Speciality.Companion.name
import com.meetingdoctors.chat.domain.DOCTOR_ROLE_DOCTOR
import com.meetingdoctors.chat.domain.DOCTOR_ROLE_MEDICAL_SUPPORT
import com.meetingdoctors.chat.domain.entitesextensions.getPendingMessageCount
import com.meetingdoctors.chat.domain.entitesextensions.isSaturated
import com.meetingdoctors.chat.domain.entities.Doctor
import com.meetingdoctors.chat.domain.entities.Speciality
import com.meetingdoctors.chat.fcm.MDFirebaseMessagingService.Companion.hidePendingMessagesNotification
import com.meetingdoctors.chat.fcm.MDFirebaseMessagingService.Companion.showPendingMessagesNotification
import com.meetingdoctors.chat.helpers.SystemHelper.Companion.isConnected
import com.meetingdoctors.chat.net.ChatSocket
import com.meetingdoctors.chat.net.ServerInterface
import com.meetingdoctors.chat.views.base.BaseLinearLayout
import com.meetingdoctors.chat.views.configurationmodel.ProfessionalConfigListModel
import com.meetingdoctors.chat.views.relationships.InvitationCodeDialog
import com.meetingdoctors.chat.views.relationships.InvitationCodeDialogActions
import kotlinx.android.synthetic.main.mediquo_layout_doctor_list.view.*


/**
 * Created by Héctor Manrique on 4/19/21.
 */

class ProfessionalList @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : BaseLinearLayout(context, attrs, defStyleAttr), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var doctorChatAdapter: DoctorChatAdapter
    private var pushBroadcastReceiver: PushBroadcastReceiver? = null
    private var chatSocket: ChatSocket? = null
    private var invitationCodeContainer: View? = null
    private var attrIncludedProfessionals = 0
    private var attrExcludedProfessionals = 0
    private var attrEnableSwipeRefresh = true
    private var listLoaded = false
    private var unreadMessageCount: Long = -1

    init {
        initialize(attrs)
    }

    private fun initialize(attributeSet: AttributeSet?) {
        if (attributeSet != null) {
            val attributes = context.theme.obtainStyledAttributes(attributeSet,
                    R.styleable.ProfessionalList,
                    0,
                    0)
            try {
                attrIncludedProfessionals = attributes.getInteger(R.styleable.ProfessionalList_includedSpecialities, 0)
                attrExcludedProfessionals = attributes.getInteger(R.styleable.ProfessionalList_excludedSpecialities, 0)
                attrEnableSwipeRefresh = attributes.getBoolean(R.styleable.ProfessionalList_enableSwipeRefresh, true)
            } finally {
                attributes.recycle()
            }
        }
        View.inflate(context, R.layout.mediquo_layout_doctor_list, this)

        listview?.isNestedScrollingEnabled = true

        doctorChatAdapter = DoctorChatAdapter((context as Activity))
        listview?.adapter = doctorChatAdapter
        listview?.onItemClickListener = AdapterView.OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            if (Repository.instance?.isAuthenticated() != true) return@OnItemClickListener
            val doctor = doctorChatAdapter.getItem(position)
            if (doctor != null) {
                if (Repository.instance?.getUserData()?.status != Constants.USER_STATUS_ACTIVE.toLong() &&
                        (doctor.app_role?.id == DOCTOR_ROLE_DOCTOR ||
                                doctor.app_role?.id == DOCTOR_ROLE_MEDICAL_SUPPORT)) {
                    if (professionalListListener != null) {
                        professionalListListener?.onProfessionalClick(
                                doctor.id,
                                name(doctor.speciality?.id),
                                false,
                                false)
                    }
                } else {
                    var launchChat = true
                    if (professionalListListener != null) {
                        if (professionalListListener?.onProfessionalClick(
                                        doctor.id,
                                        name(doctor.speciality?.id),
                                        true,
                                        doctor.isSaturated()) != true) {
                            launchChat = false
                        }
                    }
                    if (doctor.getPendingMessageCount() > 0 && doctor.getPendingMessageCount().toLong() ==
                            Repository.instance?.getPendingMessagesCount()) {
                        hidePendingMessagesNotification(context)
                    }
                    if (launchChat) {
                        launchChat(context, doctor.hash)
                    }
                }
            }
        }
        professional_list_invitation_code_button?.setOnClickListener { v: View? -> sendInvitationCode() }
        pushBroadcastReceiver = PushBroadcastReceiver()
        chatSocket = ChatSocket((context as Activity))
        chatSocket?.let {
            it.setOnConnectListener(OnConnectListener())
            it.setOnDisconnectListener(OnDisconnectListener())
            it.setOnDoctorListListener(OnDoctorListListener())
        }

        if (attrEnableSwipeRefresh && isConnected(context)) {
            swipe_refresh_layout?.setOnRefreshListener(this)
            swipe_refresh_layout?.isRefreshing = true
        } else {
            swipe_refresh_layout?.isEnabled = false
        }
    }

    override fun onReady() {
        Log.d("ProfessionalList", "onReady()")
        super.onReady()
        if (MeetingDoctorsClient.instance?.isAuthenticated != true) {
            swipe_refresh_layout?.isRefreshing = false
            doctorChatAdapter.clearDoctors()
            return
        }
        Repository.instance?.getFilteredAndSortedDoctors(attrIncludedProfessionals, attrExcludedProfessionals)?.let {
            doctorChatAdapter.setDoctors(it)
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(pushBroadcastReceiver!!, IntentFilter(context.getString(R.string.meetingdoctors_local_broadcast_gcm_message_received)))
        chatSocket?.resume()
    }

    override fun onPause() {
        Log.d("ProfessionalList", "onPause()")
        super.onPause()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(pushBroadcastReceiver!!)
        chatSocket?.pause()
    }

    override fun onRefresh() {
        Log.d("ProfessionalList", "onRefresh()")
        if (MeetingDoctorsClient.instance!!.isAuthenticated) {
            Repository.instance!!.initialize(object : ServerInterface.ResponseListener {
                override fun onResponse(error: Throwable?, statusCode: Int, data: Any?) {
                    swipe_refresh_layout?.isRefreshing = true
                    chatSocket!!.getDoctorList()
                }
            })
        } else {
            swipe_refresh_layout?.isRefreshing = false
            doctorChatAdapter.clearDoctors()
        }
    }

    fun sendMessageToRoom(message: String, roomId: String?, hash: String?) {
        var message = message
        message = message.trim { it <= ' ' }
        val doctors: List<Doctor>? = Repository.instance?.getFilteredAndSortedDoctors(attrIncludedProfessionals, attrExcludedProfessionals)
        var doctorSpeciality: Speciality? = null

        doctors?.let {
            for (doctor in doctors) {
                if (doctor.hash.equals(hash, ignoreCase = true)) {
                    doctorSpeciality = doctor.speciality
                }
            }
        }

        if (chatSocket?.isReady() != true) {
            Toast.makeText(context, context.getString(R.string.meetingdoctors_not_connected), Toast.LENGTH_SHORT).show()
            return
        }
        roomId?.let {
            val roomIntValue = roomId.toInt()
            val messageId = chatSocket?.sendMessage(roomIntValue, message)
            if (hash != null && messageId != null) {
                Repository.instance?.getTracking()?.messageSent(
                        roomIntValue,
                        hash,
                        name(doctorSpeciality?.id),
                        "string",
                        messageId,
                        message
                )
            }

        }

    }

    fun includeSpecialities(specialities: String?) {
        if (specialities != null) {
            attrIncludedProfessionals = 0
            for (speciality in specialities.split(",".toRegex()).toTypedArray()) {
                val trimmedSpeciality = speciality.trim { it <= ' ' }
                if (trimmedSpeciality.isNotEmpty()) {
                    attrIncludedProfessionals = attrIncludedProfessionals or flag(speciality)
                }
            }
            populateList()
        }
    }

    fun excludeSpecialities(specialities: String?) {
        if (specialities != null) {
            attrExcludedProfessionals = 0
            for (speciality in specialities.split(",".toRegex()).toTypedArray()) {
                val trimmedSpeciality = speciality.trim { it <= ' ' }
                if (!trimmedSpeciality.isEmpty()) {
                    attrExcludedProfessionals = attrExcludedProfessionals or flag(speciality)
                }
            }
            populateList()
        }
    }

    private inner class OnConnectListener : ChatSocket.OnConnectListener {
        override fun onConnect() {
            Log.d("ProfessionalList", "OnConnectListener.onConnect()")
            (context as Activity).runOnUiThread { chatSocket!!.getDoctorList() }
        }
    }

    private inner class OnDisconnectListener : ChatSocket.OnDisconnectListener {
        override fun onDisconnect() {
            Log.d("ProfessionalList", "OnDisconnectListener.onDisconnect()")
            (context as Activity).runOnUiThread {
                swipe_refresh_layout?.isRefreshing = false
                doctorChatAdapter.setSocketConnected(false)
            }
        }
    }

    private inner class OnDoctorListListener : ChatSocket.OnDoctorListListener {
        override fun onDoctorList() {
            Log.d("ProfessionalList", "OnDoctorListListener.onDoctorList()")
            (context as Activity).runOnUiThread {
                swipe_refresh_layout?.isRefreshing = false
                doctorChatAdapter.setSocketConnected(true)
                populateList()
            }
        }
    }

    private fun populateList() {
        val doctors: List<Doctor>? = Repository.instance?.getFilteredAndSortedDoctors(attrIncludedProfessionals, attrExcludedProfessionals)
        doctors?.let {
            doctorChatAdapter.setDoctors(doctors)
        }
        if (doctors?.isEmpty() == true && resources.getBoolean(R.bool.meetingdoctors_show_invitation_code_container)) {
            listview?.visibility = View.INVISIBLE
            invitationCodeContainer?.visibility = View.VISIBLE
            return
        }
        if (professionalListListener != null && doctors?.isNotEmpty() == true) {
            if (!listLoaded) {
                listLoaded = true
                professionalListListener?.onListLoaded()
            }
            var unreadMessageCount = 0
            for (doctor in doctors) {
                unreadMessageCount += doctor.getPendingMessageCount()
            }
            if (this@ProfessionalList.unreadMessageCount != unreadMessageCount.toLong()) {
                this@ProfessionalList.unreadMessageCount = unreadMessageCount.toLong()
                professionalListListener?.onUnreadMessageCountChange(unreadMessageCount.toLong())
            }
        }
    }

    fun refreshDoctorsList() {
        setDoctorsListVisible()
        onRefresh()
    }

    fun setDoctorsListVisible() {
        listview?.visibility = View.VISIBLE
        invitationCodeContainer?.visibility = View.INVISIBLE
    }

    private fun sendInvitationCode() {
        val invitationCodeDialog = InvitationCodeDialog()
        invitationCodeDialog.showInvitationCodeDialog(context, object : InvitationCodeDialogActions {
            override fun sendInvitationCode(code: String) {
                setDoctorsListVisible()
                onRefresh()
            }

            override fun dismissInvitationCodeDialog() {}
        })
    }

    private inner class PushBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("ProfessionalList", "PushBroadcastReceiver.onReceive()")
            chatSocket?.getDoctorList()
            try {
                if (intent.hasExtra("roomId")) {
                    showPendingMessagesNotification(getContext(), intent.getIntExtra("roomId", 0), null, null)
                    return
                }
            } catch (ignored: Exception) {
            }
            showPendingMessagesNotification(getContext())
        }
    }

    @Keep
    interface ProfessionalListListener {
        fun onProfessionalClick(professionalId: Long, speciality: String?, hasAccess: Boolean, isSaturated: Boolean): Boolean
        fun onListLoaded()
        fun onUnreadMessageCountChange(unreadMessageCount: Long)
    }

    private var professionalListListener: ProfessionalListListener? = null
    fun setProfessionalListListener(professionalListListener: ProfessionalListListener?) {
        this.professionalListListener = professionalListListener
    }

    fun setConfigModel(configModel: ProfessionalConfigListModel?) {
        doctorChatAdapter.setConfigModel(configModel)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        when (heightMode) {
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(widthSize, heightSize)
            }
            MeasureSpec.AT_MOST -> {
                setMeasuredDimension(widthSize, Math.min(heightSize, getListHeight()))
            }
            else -> {
                setMeasuredDimension(widthSize, getListHeight())
            }
        }
    }

    fun getListHeight(): Int {
        if (doctorChatAdapter == null || doctorChatAdapter.count == 0) {
            return 0
        }
        var totalHeight = 0
        for (i in 0 until doctorChatAdapter.count) {
            val listItem = doctorChatAdapter.getView(i, null, listview)
            listItem?.measure(0, 0)
            totalHeight += listItem?.measuredHeight ?: 0
        }
        return totalHeight + (listview?.dividerHeight ?: 0) * (doctorChatAdapter.count - 1)
    }

    fun setDividerView(view: ViewGroup?) {
        doctorChatAdapter.setDividerView(view)
    }

    fun setDisabledProfessionalColor(disabledProfessionalColor: Int) {
        doctorChatAdapter.setDisabledProfessionalColor(disabledProfessionalColor)
    }

    fun setBeforeDividerSpecialityText(beforeDividerSpecialityText: String?) {
        doctorChatAdapter.setBeforeDividerSpecialityText(beforeDividerSpecialityText)
    }
}