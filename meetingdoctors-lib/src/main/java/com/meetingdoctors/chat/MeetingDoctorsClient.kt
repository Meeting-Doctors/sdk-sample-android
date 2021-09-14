package com.meetingdoctors.chat

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.annotation.Keep
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.meetingdoctors.chat.activities.base.BaseActivity
import com.meetingdoctors.chat.data.Cache
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.data.repositories.CustomerResponseListener
import com.meetingdoctors.chat.data.webservices.CustomerSdkBuildMode
import com.meetingdoctors.chat.data.webservices.entities.UpdateProfileInfoBody
import com.meetingdoctors.chat.domain.entitesextensions.getLastMessage
import com.meetingdoctors.chat.domain.entities.Setup
import com.meetingdoctors.chat.fcm.MDFirebaseMessagingService
import com.meetingdoctors.chat.helpers.NpsHelper.isNpsRequestCompletedByUser
import com.meetingdoctors.chat.helpers.StringHelper
import com.meetingdoctors.chat.helpers.SystemHelper
import com.meetingdoctors.chat.locale.LocaleHelper
import com.meetingdoctors.chat.net.ServerInterface
import com.meetingdoctors.chat.views.nps.NpsRatingDialogActions
import com.meetingdoctors.chat.views.nps.NpsRequestDialogAdapterImpl
import com.meetingdoctors.chat.views.relationships.InvitationCodeDialog
import com.meetingdoctors.chat.views.relationships.InvitationCodeDialogActions
import com.meetingdoctors.chat.views.toolbar.MenuBaseAction
import com.meetingdoctors.chat.views.toolbar.MenuBaseCustomViewListener
import com.meetingdoctors.chat.views.toolbar.MenuBaseCustomViewWrapper
import com.meetingdoctors.mdsecure.sharedpref.MDSecurePreferencesManager.init
import com.meetingdoctors.mdsecure.sharedpref.OnResetDataListener
import com.meetingdoctors.mdsecure.sharedpref.OnSecureInitialization
import com.meetingdoctors.mdsecure.sharedpref.encryption.MDEncryptionException
import java.util.*


class MeetingDoctorsClient private constructor(application: Application,
                                               apiKey: String,
                                               targetEnvironment: CustomerSdkBuildMode,
                                               encryptionEnabled: Boolean,
                                               encryptionPassword: String?,
                                               locale: Locale? = null) {

    @Keep
    companion object {

        var instance: MeetingDoctorsClient? = null
            private set
        var videoCallRequestListener: OnVideoCallRequest? = null
            private set

        @JvmStatic
        fun newInstance(application: Application,
                        apiKey: String,
                        targetEnvironment: CustomerSdkBuildMode,
                        isSharedPreferencesEncrypted: Boolean,
                        encryptionpassword: String?,
                        locale: Locale? = null): MeetingDoctorsClient? {
            if (instance == null) {
                instance = MeetingDoctorsClient(application,
                        apiKey,
                        targetEnvironment,
                        isSharedPreferencesEncrypted,
                        encryptionpassword,
                        locale)
            }
            return instance
        }

    }

    private var application: Application? = null
    private var menuBaseCustomViewWrapper: MenuBaseCustomViewWrapper? = null

    init {
        try {
            this.application = application
            locale?.let {
                LocaleHelper.setLocale(application.applicationContext, locale)
            }
            initMDSecureLibrary(apiKey, targetEnvironment, encryptionEnabled, encryptionPassword)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetInstance() {
        instance = null
    }

    private fun initMDSecureLibrary(apiKey: String, targetEnvironment: CustomerSdkBuildMode, isEncryptionEnabled: Boolean, password: String?) {
        try {
            init(application!!.applicationContext, password!!, object : OnSecureInitialization {
                override fun onSuccess() {
                    Log.d("MDSecure", "Initialization Successful")
                    initRepository(apiKey, targetEnvironment)
                }

                override fun onFailure(exception: Exception) {
                    Log.e("MDSecure", "Initialization Error :-(")
                }
            }, isEncryptionEnabled)
        } catch (e: MDEncryptionException) {
            e.printStackTrace()
        }
    }

    private fun initRepository(apiKey: String, targetEnvironment: CustomerSdkBuildMode) {
        application?.applicationContext?.let {
            val repository = Repository.getInstance(it, targetEnvironment, apiKey)
            repository.setApiKey(apiKey)
            repository.getSetup(object : Repository.GetSetupResponseListener {
                override fun onResponse(error: Throwable?, setup: Setup?) {
                }
            })
            if (repository.getPushToken() == null) {
                FirebaseMessaging.getInstance().token
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful && task.result != null) {
                                val token = task.result?.toString()
                                Log.d("FCM", "FirebaseInstanceId.getInstanceId() token[$token]")
                                token?.let { it1 -> Repository.instance?.setPushToken(it1) }
                            } else {
                                Log.d("FCM", "FirebaseInstanceId.getInstanceId() error")
                            }
                        }
            }
        }
    }

    interface AuthenticationListener {
        fun onAuthenticated()
        fun onAuthenticationError(throwable: Throwable)
    }

    fun authenticate(userToken: String?, authenticationListener: AuthenticationListener?) {
        try {
            userToken?.let {
                Repository.instance?.authenticate(it, object : ServerInterface.ResponseListener {
                    override fun onResponse(error: Throwable?, statusCode: Int, data: Any?) {
                        if (statusCode == 200 && error == null) {
                            authenticationListener?.onAuthenticated()
                        } else {
                            authenticationListener?.onAuthenticationError(error!!)
                        }
                    }
                })
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deauthenticate() {
        try {
            Repository.instance?.deauthenticate(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deauthenticate(resetDataListener: OnResetDataListener) {
        try {
            Repository.instance?.deauthenticate(resetDataListener);
        } catch (e: Exception) {
            e.printStackTrace();
        }
    }

    fun getMenuView(): View? {
        return menuBaseCustomViewWrapper?.getCustomView()
    }

    fun getMenuBaseCustomViewWrapper(): MenuBaseCustomViewWrapper? {
        return menuBaseCustomViewWrapper
    }

    fun setMenuAction(action: MenuBaseAction?) {
        menuBaseCustomViewWrapper = MenuBaseCustomViewWrapper(action!!)
    }

    fun saveMenuView(v: View, titleBar: String, @Nullable menuBaseCustomViewListener: MenuBaseCustomViewListener) {
        menuBaseCustomViewWrapper?.let {
            it.saveCustomView(v)
                    .setActionCallback(menuBaseCustomViewListener)
                    .saveCustomTitle(titleBar)
        }
    }


    interface GetUnreadMessageCountResponseListener {
        fun onResponse(count: Long)
        fun onError()
    }

    fun getUnreadMessageCount(responseListener: GetUnreadMessageCountResponseListener) {
        try {
            Repository.instance?.getUnreadMessageCount(object : Repository.GetUnreadMessageCountResponseListener {
                override fun onResponse(count: Long) {
                    responseListener.onResponse(count)
                }

                override fun onError(e: Exception?) {
                    responseListener.onError()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val isAuthenticated: Boolean
        get() = Repository.instance?.isAuthenticated() ?: false

    fun launchNpsDialog(context: Context?, npsActionListener: NpsRatingDialogActions?) {
        val npsDialog = NpsRequestDialogAdapterImpl()
        npsDialog.showNpsDialog(context!!, npsActionListener)
    }

    fun launchInvitationCodeDialog(context: Context?, invitationCodeDialogActions: InvitationCodeDialogActions?) {
        val invitationCodeDialog = InvitationCodeDialog()
        invitationCodeDialog.showInvitationCodeDialog(context!!, invitationCodeDialogActions)
    }

    fun openChatWithSpeciality(context: Context?, speciality: String?): Boolean {
        try {
            val doctor = Repository.instance?.getDoctorBySpeciality(speciality, true)
            if (doctor != null && context != null) {
                BaseActivity.launchChat(context, doctor.hash)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun openChatWithBySpecialityId(context: Context?, specialityId: Int): Boolean {
        try {
            val doctor = Repository.instance?.getDoctorBySpecialityId(specialityId, true)
            if (doctor != null && context != null) {
                BaseActivity.launchChat(context, doctor.hash)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun openChatWithProfessional(context: Context?, professionalId: Long): Boolean {
        return openChatWithProfessionalList(context, professionalId.toString())
    }

    fun openChatWithProfessionalList(context: Context?, professionalList: String?): Boolean {
        try {
            val doctor = Repository.instance?.getDoctorById(StringHelper.parseLongArray(professionalList), true)
            if (doctor != null && context != null) {
                BaseActivity.launchChat(context, doctor.hash)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun openChatWithSpecialityAndSendMessage(context: Context?, speciality: String?, outgoingMessage: String?): Boolean {
        try {
            val doctor = Repository.instance?.getDoctorBySpeciality(speciality, true)
            if (doctor != null && context != null) {
                BaseActivity.launchChatAndSendMessage(context, doctor.hash, outgoingMessage)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun openChatWithProfessionalAndSendMessage(context: Context?, professionals: String?, outgoingMessage: String?): Boolean {
        try {
            val doctor = Repository.instance?.getDoctorById(StringHelper.parseLongArray(professionals), true)
            if (doctor != null && context != null) {
                BaseActivity.launchChatAndSendMessage(context, doctor.hash, outgoingMessage)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun openChatWithSpecialityAndReceiveMessage(context: Context?, speciality: String?, outgoingMessage: String?): Boolean {
        try {
            val doctor = Repository.instance?.getDoctorBySpeciality(speciality, false)
            if (doctor != null && context != null) {
                BaseActivity.launchChatAndReceiveMessage(context, doctor.hash, outgoingMessage)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun openChatWithProfessionalAndReceiveMessage(context: Context?, professionals: String?, outgoingMessage: String?): Boolean {
        try {
            val doctor = Repository.instance?.getDoctorById(StringHelper.parseLongArray(professionals), false)
            if (doctor != null && context != null) {
                BaseActivity.launchChatAndReceiveMessage(context, doctor.hash, outgoingMessage)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun receiveMessageFromSpeciality(context: Context?, speciality: String?, incomingMessage: String?): Boolean {
        try {
            val doctor = Repository.instance?.getDoctorBySpeciality(speciality, false)
            if (doctor != null) {
                BaseActivity.receiveMessage(context, doctor.hash, incomingMessage)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun receiveMessageFromProfessional(context: Context?, professionals: String?, incomingMessage: String?): Boolean {
        try {
            val doctor = Repository.instance?.getDoctorById(StringHelper.parseLongArray(professionals), false)
            if (doctor != null) {
                BaseActivity.receiveMessage(context, doctor.hash, incomingMessage)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun receiveFirstMessageFromSpeciality(context: Context?, speciality: String?, incomingMessage: String?): Boolean {
        try {
            val doctor = Repository.instance?.getDoctorBySpeciality(speciality, false)
            if (doctor != null && doctor.getLastMessage() == null) {
                BaseActivity.receiveMessage(context, doctor.hash, incomingMessage)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun receiveFirstMessageFromProfessional(context: Context?, professionals: String?, incomingMessage: String?): Boolean {
        try {
            val doctor = Repository.instance?.getDoctorById(StringHelper.parseLongArray(professionals), false)
            if (doctor != null && doctor.getLastMessage() == null) {
                BaseActivity.receiveMessage(context, doctor.hash, incomingMessage)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun updateProfileInfo(firstName: String?,
                          lastName: String?,
                          gender: Long?,
                          birthDate: String?,
                          listener: CustomerResponseListener) {
        Repository.instance?.updateProfileInfo(
                UpdateProfileInfoBody(firstName!!,
                        lastName!!,
                        gender!!,
                        birthDate!!),
                listener)
    }

    fun openMedicalHistory(context: Context?): Boolean {
        try {
            return if (context != null) {
                BaseActivity.launchMedicalHistory(context)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    val referrer: String?
        get() {
            try {
                return Repository.instance?.getReferrer()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

    val installationGuid: String?
        get() {
            try {
                return Repository.instance?.getInstallationGuid()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

    val languageCode: String?
        get() = SystemHelper.getLanguageIso()

    val countryCode: String?
        get() = SystemHelper.getCountryIso(application!!.applicationContext)

    val isNpsRequestCompletedStatus: Boolean
        get() = isNpsRequestCompletedByUser()

    var returnIntent: Intent? = null

    interface RatingRequestListener {
        fun onActiveRatingRequest()
        fun onPassiveRatingRequest()
    }

    var ratingRequestListener: RatingRequestListener? = null

    fun setCollegiateNumbersVisibility(visible: Boolean) {
        Repository.instance?.setCollegiateNumbersVisibility(visible)
    }

    fun onFirebaseMessageReceived(remoteMessage: RemoteMessage, splashActivityClass: Class<out Activity?>?): Boolean {
        return if (application?.applicationContext != null) {
            MDFirebaseMessagingService.onMessageReceived(application!!.applicationContext, remoteMessage, splashActivityClass)
        } else {
            false
        }
    }

    fun onNewTokenReceived(@NonNull token: String) {
        Repository.instance?.registerPushTokenFromNewToken(token)
    }

    fun setVideoCallRequestListener(videoCallRequest: OnVideoCallRequest) {
        videoCallRequestListener = videoCallRequest
    }

    fun isMedicalHistoryActive(context: Context): Boolean {
        return Cache.getSetup(context)?.medicalHistory?.active ?: true
    }

    interface OnVideoCallRequest {
        fun perform1to1VideoCall(professionalHash: String?, doctorName: String?, context: Context?, videoCallRequestDoneListener: OnVideoCall1to1RequestDone?)
        fun performCancelVideoCall(context: Context?, message: String?, videoCallRequestDoneListener: OnVideoCall1to1RequestDone?, doctorName: String?)
        fun hasProfessionalAssignedVideoCall(professionalHash: String?): Boolean
    }

    interface OnVideoCall1to1RequestDone {
        fun performRequestDoneAction(roomId: String?)
        fun performSendCancelledCallMessage(roomId: String?, hash: String?)
        fun performRequestCancelledAction()
    }
}
