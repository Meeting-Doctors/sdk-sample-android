package com.meetingdoctors.chat.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.meetingdoctors.chat.BuildConfig
import com.meetingdoctors.chat.MeetingDoctorsClient
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.Tracking
import com.meetingdoctors.chat.data.Cache.Companion.clear
import com.meetingdoctors.chat.data.Cache.Companion.getDoctors
import com.meetingdoctors.chat.data.Cache.Companion.getSetup
import com.meetingdoctors.chat.data.Cache.Companion.getUserData
import com.meetingdoctors.chat.data.Cache.Companion.putDoctors
import com.meetingdoctors.chat.data.Cache.Companion.putSetup
import com.meetingdoctors.chat.data.Cache.Companion.putUserData
import com.meetingdoctors.chat.data.Speciality.Companion.id
import com.meetingdoctors.chat.data.repositories.*
import com.meetingdoctors.chat.data.webservices.*
import com.meetingdoctors.chat.data.webservices.endpoints.*
import com.meetingdoctors.chat.data.webservices.entities.CheckVerificationCodeResponse
import com.meetingdoctors.chat.data.webservices.entities.NpsBody
import com.meetingdoctors.chat.data.webservices.entities.SendInvitationCodeBody
import com.meetingdoctors.chat.data.webservices.entities.UpdateProfileInfoBody
import com.meetingdoctors.chat.data.webservices.toolkit.OkHttpClientFactory
import com.meetingdoctors.chat.domain.DOCTOR_ROLE_DOCTOR
import com.meetingdoctors.chat.domain.DOCTOR_ROLE_MEDICAL_SUPPORT
import com.meetingdoctors.chat.domain.DOCTOR_STATUS_ONLINE
import com.meetingdoctors.chat.domain.entitesextensions.getPendingMessageCount
import com.meetingdoctors.chat.domain.entitesextensions.isSaturated
import com.meetingdoctors.chat.domain.entities.Doctor
import com.meetingdoctors.chat.domain.entities.Prescription
import com.meetingdoctors.chat.domain.entities.Setup
import com.meetingdoctors.chat.domain.entities.UserData
import com.meetingdoctors.chat.domain.exceptions.PrescriptionException
import com.meetingdoctors.chat.domain.executor.SchedulersFacadeImpl
import com.meetingdoctors.chat.domain.repositories.*
import com.meetingdoctors.chat.domain.usecase.AreReportsOptionEnabledUseCase
import com.meetingdoctors.chat.domain.usecase.DownloadFileUseCase
import com.meetingdoctors.chat.domain.usecase.GetPrescriptionUseCase
import com.meetingdoctors.chat.fcm.MDFirebaseMessagingService
import com.meetingdoctors.chat.helpers.SystemHelper.Companion.getCountryIso
import com.meetingdoctors.chat.helpers.SystemHelper.Companion.getLanguageIso
import com.meetingdoctors.chat.net.ServerInterface
import com.meetingdoctors.chat.net.ServerInterface.Companion.getInstance
import com.meetingdoctors.chat.presentation.entitiesextensions.formatToPrescriptionDate
import com.meetingdoctors.chat.presentation.entitiesextensions.formatToPrescriptionHour
import com.meetingdoctors.chat.views.nps.NpsRatingDialogActions
import com.meetingdoctors.mdsecure.sharedpref.MDSecurePreferencesManager.getSecurePreferences
import com.meetingdoctors.mdsecure.sharedpref.MDSecurePreferencesManager.resetPreferences
import com.meetingdoctors.mdsecure.sharedpref.OnResetDataListener
import com.meetingdoctors.mdsecure.sharedpref.preference.SharedPreferencesHelper.clear
import com.meetingdoctors.mdsecure.sharedpref.preference.SharedPreferencesHelper.getBoolean
import com.meetingdoctors.mdsecure.sharedpref.preference.SharedPreferencesHelper.getLong
import com.meetingdoctors.mdsecure.sharedpref.preference.SharedPreferencesHelper.getString
import com.meetingdoctors.mdsecure.sharedpref.preference.SharedPreferencesHelper.put
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.*

/**
 * Created by Héctor Manrique on 4/14/21.
 */

class Repository(context: Context, environmentTarget: CustomerSdkBuildMode, apiKey: String?) {
    private val doctors = ArrayList<Doctor>()
    private lateinit var context: Context
    private lateinit var environmentTarget: CustomerSdkBuildMode
    private var socket: Socket? = null
    private var setup: Setup? = null
    private val cacheDir: File
    private val customerApi: CustomerApi
    private val chatApi: ChatApi
    private val notificationsApi: NotificationsApi
    private val prescriptionApi: PrescriptionApi
    private val fileApi: FileApi
    private val customerRepository: CustomerRepository
    private val medicalHistoryRepository: MedicalHistoryRepository
    private val consultationsRepository: ConsultationsRepository
    private val prescriptionRepository: PrescriptionRepository
    private val fileRepository: FileRepository
    private lateinit var getPrescriptionUseCase: GetPrescriptionUseCase
    private lateinit var downloadFileUseCase: DownloadFileUseCase
    private lateinit var areReportsOptionEnabledUseCase: AreReportsOptionEnabledUseCase

    /////////////////////// TRACKING ///////////////////////
    private val tracking: Tracking
    fun getTracking(): Tracking {
        return tracking
    }

    companion object {
        //region DATA
        var instance: Repository? = null
        private const val API_KEY_KEY = "apiKey"
        private const val REFERRER_KEY = "referrer"
        private const val USER_TOKEN_KEY = "userToken"
        private const val USER_HASH_KEY = "userHash"
        private const val SESSION_TOKEN_KEY = "sessionToken"
        private const val COLLEGIATE_NUMBERS_VISIBILITY_KEY = "collegiateNumbersVisibility"
        private const val MESSAGE_FROM_DOCTOR_COUNTER_KEY = "messageFromDoctorCounter"
        private const val INSTALLATION_GUID_KEY = "installationGuid"
        private const val OS_NAME = "android"
        private const val LATITUDE_KEY = "latitude"
        private const val LONGITUDE_KEY = "longitude"
        private const val PUSH_TOKEN_KEY = "pushToken"
        fun getInstance(
            context: Context,
            environmentTarget: CustomerSdkBuildMode,
            apiKey: String?
        ): Repository {
            if (instance == null) {
                instance = Repository(context, environmentTarget, apiKey)
            }
            instance?.let {
                it.context = context
                it.environmentTarget = environmentTarget
            }
            return instance as Repository
        }

    }

    //region  INSTANCE
    init {
        getInstance(context)
        tracking = Tracking(context)
        cacheDir = context.cacheDir
        setApiKey(apiKey)
        val medicalHistoryRetrofit =
            generateRetrofitBuilder(getConsultationsCustomerServer(environmentTarget))
        val medicalHistoryApi = medicalHistoryRetrofit
            .create(MedicalHistoryApi::class.java)
        val consultationRetrofit =
            generateRetrofitBuilder(getConsultationsCustomerServer(environmentTarget))
        val consultationsApi = consultationRetrofit
            .create(ConsultationsApi::class.java)
        val customerRetrofit = generateRetrofitBuilder(getCustomerServer(environmentTarget))
        customerApi = customerRetrofit
            .create(CustomerApi::class.java)
        val chatRetrofit = generateRetrofitBuilder(getChatServer(environmentTarget))
        chatApi = chatRetrofit
            .create(ChatApi::class.java)
        val notificationsRetrofit =
            generateRetrofitBuilder(getNotificationsServer(environmentTarget))
        notificationsApi = notificationsRetrofit
            .create(NotificationsApi::class.java)
        val prescriptionRetrofit = generateRetrofitBuilder(getPrescriptionServer(environmentTarget))
        prescriptionApi = prescriptionRetrofit
            .create(PrescriptionApi::class.java)

        // We need to pass some valid url as baseUrl to instantiate retrofit, if this value is empty thow and error
        val fileRetrofit = generateBasicRetrofit(getPrescriptionServer(environmentTarget))
        fileApi = fileRetrofit
            .create(FileApi::class.java)
        customerRepository = CustomerRepositoryImpl(customerApi, this)
        medicalHistoryRepository = MedicalHistoryRepositoryImpl(medicalHistoryApi, this)
        consultationsRepository = ConsultationsRepositoryImpl(consultationsApi, this)
        prescriptionRepository = PrescriptionRepositoryImpl(prescriptionApi, this)
        fileRepository = FileRespositoryImpl(fileApi, context)
        initUseCases()
    }

    private fun generateRetrofitBuilder(baseUrl: String): Retrofit {
        val okHttpClient = OkHttpClientFactory(this).createOkHttpClient()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private fun generateBasicRetrofit(baseUrl: String): Retrofit {
        val okHttpClient = OkHttpClient.Builder().cache(null).build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private fun clear() {
        MDFirebaseMessagingService.hidePendingMessagesNotification(context)
        closeSocket()
        clearCaches()
    }

    private fun clear(onResetDataListener: OnResetDataListener?) {
        MDFirebaseMessagingService.hidePendingMessagesNotification(context)
        logout(onResetDataListener)
    }

    private fun closeSocket() {
        if (socket != null) {
            socket!!.close()
            socket = null
        }
    }

    private fun clearCaches() {
        clearPushToken()
        clear(context)
        clearUserToken()
        clearUserHash()
        clearSessionToken()
        doctors.clear()
        userData = null
    }

    fun getMedicalHistoryRepository(): MedicalHistoryRepository {
        return medicalHistoryRepository
    }

    private fun initUseCases() {
        val schedulersFacade = SchedulersFacadeImpl.getInstance()
        getPrescriptionUseCase = GetPrescriptionUseCase(schedulersFacade!!)
        downloadFileUseCase = DownloadFileUseCase(schedulersFacade)
        areReportsOptionEnabledUseCase = AreReportsOptionEnabledUseCase(schedulersFacade)
    }

    fun getPrescriptionUseCase(): GetPrescriptionUseCase {
        return getPrescriptionUseCase
    }

    fun getDownloadFileUseCase(): DownloadFileUseCase {
        return downloadFileUseCase
    }

    fun getAreReportsOptionEnabledUseCase(): AreReportsOptionEnabledUseCase {
        return areReportsOptionEnabledUseCase
    }

    fun getEnvironmentTarget(): CustomerSdkBuildMode {
        return environmentTarget
    }

    fun getSocket(): Socket? {
        return socket
    }

    fun setSocket(socket: Socket): Socket {
        return socket.also { this.socket = it }
    }

    fun getCacheDir(): File {
        return cacheDir
    }

    fun getApiKey(): String? {
        return getString(API_KEY_KEY)
    }

    fun setApiKey(apiKey: String?) {
        put(API_KEY_KEY, apiKey)
    }

    fun setReferrer(referrer: String?) {
        put(REFERRER_KEY, referrer)
    }

    fun getReferrer(): String? {
        return getString(REFERRER_KEY)
    }

    fun getUserToken(): String? {
        return getString(USER_TOKEN_KEY)
    }

    fun setUserToken(userToken: String?) {
        put(USER_TOKEN_KEY, userToken)
    }

    fun clearUserToken() {
        clear(USER_TOKEN_KEY)
    }

    fun getUserHash(): String? {
        return getString(USER_HASH_KEY)
    }

    fun setUserHash(userHash: String) {
        put(USER_HASH_KEY, userHash)
    }

    fun clearUserHash() {
        clear(USER_HASH_KEY)
    }

    fun getSessionToken(): String? {
        return getString(SESSION_TOKEN_KEY)
    }

    fun setSessionToken(sessionToken: String) {
        put(SESSION_TOKEN_KEY, sessionToken)
    }

    private fun clearSessionToken() {
        clear(SESSION_TOKEN_KEY)
    }

    fun setCollegiateNumbersVisibility(visible: Boolean) {
        put(COLLEGIATE_NUMBERS_VISIBILITY_KEY, visible)
    }

    fun getCollegiateNumbersVisibility(): Boolean {
        return getBoolean(COLLEGIATE_NUMBERS_VISIBILITY_KEY, false)
    }

    //endregion
    //region BASIC DATA
    fun getMessageFromDoctorCounter(): Long {
        return getLong(MESSAGE_FROM_DOCTOR_COUNTER_KEY)
    }

    //endregion
    //region SETUP
    fun setMessageFromDoctorCounter(messageFromDoctorCounter: Long) {
        put(MESSAGE_FROM_DOCTOR_COUNTER_KEY, messageFromDoctorCounter)
    }

    @SuppressLint("HardwareIds")
    fun getInstallationGuid(): String {
        var installationGuid = getSecurePreferences()?.getString(INSTALLATION_GUID_KEY, null)
        if (installationGuid == null) {
            installationGuid = UUID.nameUUIDFromBytes(
                (Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                ) +
                        Build.VERSION.RELEASE +
                        Build.MODEL +
                        getApiKey() +
                        Math.random()).toByteArray()
            ).toString()
            getSecurePreferences()?.edit()?.putString("installationGuid", installationGuid)
                ?.commit()
        }
        return installationGuid
    }

    fun initialize(responseListener: ServerInterface.ResponseListener) {
        Log.i("Repository", "initialize()")
        if (getSessionToken() == null) {
            Log.e("Repository", "initialize() error")
            responseListener.onResponse(Exception("Bad Request"), 400, null)
        } else {
            getUserData(object : ServerInterface.ResponseListener {
                override fun onResponse(error: Throwable?, statusCode: Int, data: Any?) {

                    if (error == null && statusCode == 200) {
                        Log.i("Repository", "initialize() done!")
                        responseListener.onResponse(null, 200, null)
                    } else {
                        Log.e("Repository", "initialize() error")
                        responseListener.onResponse(error, statusCode, null)
                    }
                }
            })
        }
    }

    @SuppressLint("CheckResult", "HardwareIds")
    fun getSetup(responseListener: GetSetupResponseListener) {
        Log.i("", "")
        customerApi.getSetup(
            OS_NAME,
            Build.VERSION.RELEASE,
            BuildConfig.VERSION_NAME,
            getInstallationGuid(),
            Build.MODEL,
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
            context.packageName,
            getLanguageIso(),
            getCountryIso(context)
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ acceptedSetup: Setup? ->
                setup = acceptedSetup
                putSetup(context, setup)
                responseListener.onResponse(null, setup)
            }) { throwable: Throwable? ->
                setup = getSetup(context)
                if (setup == null) {
                    responseListener.onResponse(throwable, null)
                } else {
                    responseListener.onResponse(null, setup)
                }
            }
    }

    //endregion
    //region USER DATA
    private var userData: UserData? = null

    @SuppressLint("CheckResult")
    fun getUserData(responseListener: ServerInterface.ResponseListener) {
        customerApi.getUserData(
            OS_NAME,
            Build.VERSION.RELEASE,
            BuildConfig.VERSION_NAME,
            Build.MODEL,
            getReferrer()
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ (userData1) ->
                userData = userData1
                putUserData(context, userData)
                responseListener.onResponse(null, 200, userData)
            }) { throwable: Throwable? ->
                userData = getUserData(context)
                if (userData == null) {
                    if (throwable is HttpException) {
                        responseListener.onResponse(throwable, throwable.code(), null)
                    } else {
                        responseListener.onResponse(throwable, 400, null)
                    }
                } else {
                    responseListener.onResponse(null, 200, userData)
                }
            }
    }

    fun getUserData(): UserData? {
        if (userData == null) {
            userData = getUserData(context)
        }
        return userData
    }

    @SuppressLint("CheckResult")
    fun updateProfileInfo(info: UpdateProfileInfoBody, listener: CustomerResponseListener) {
        customerRepository.updateProfileInfo(info, listener)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ (userData1) ->
                userData = userData1
                putUserData(context, userData)
                listener.onSuccessResponse()
            }) { throwable: Throwable -> listener.onErrorResponse(if (throwable.localizedMessage != null) throwable.localizedMessage else "Throwed exception updateProfileInfo()") }
    }

    //endregion
    //region RelationShips
    @SuppressLint("CheckResult")
    fun sendInvitationCode(code: String?, customerResponseListener: CustomerResponseListener) {
        customerRepository.sendRelationShipInvitationCode(
            SendInvitationCodeBody(code!!),
            customerResponseListener
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ (message) -> customerResponseListener.onSuccessResponse() }) {
                customerResponseListener.onErrorResponse(
                    "El codigo es incorrecto"
                )
            }
    }

    //endregion
    //region  AUTHENTICATION
    fun isAuthenticated(): Boolean {
        return getUserHash() != null && getSessionToken() != null && getUserData() != null
    }

    @SuppressLint("CheckResult")
    fun authenticate(userToken: String, responseListener: ServerInterface.ResponseListener) {
        if (getUserToken() != null && getUserToken() == userToken) {
            if (isAuthenticated()) {
                registerPushToken()
                responseListener.onResponse(null, 200, null)
                return
            }
        } else {
            clear()
        }
        customerApi.authenticate(getInstallationGuid(), userToken)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ checkVerificationCodeResponse: CheckVerificationCodeResponse ->
                setUserToken(userToken)
                setUserHash(checkVerificationCodeResponse.userHash)
                setSessionToken(checkVerificationCodeResponse.sessionToken)
                instance?.getUserData(object : ServerInterface.ResponseListener {
                    override fun onResponse(error: Throwable?, statusCode: Int, data: Any?) {

                        if (statusCode == 200 && error == null) {
                            registerPushToken()
                            val intent =
                                Intent(context.getString(R.string.meetingdoctors_local_broadcast_authenticate))
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                        }
                        responseListener.onResponse(error, statusCode, null)
                    }
                })
            }) { throwable ->
                responseListener.onResponse(throwable, 400, null)
            }
    }

    @SuppressLint("CheckResult")
    private fun logout(onResetDataListener: OnResetDataListener?) {
        customerRepository.expireJwt(onResetDataListener)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                closeSocket()
                clearCaches()
                MeetingDoctorsClient.instance?.resetInstance()
                resetPreferences(onResetDataListener)
                Log.i("Repository.logout()", "Logout JWT SUCCESS")
            }) { throwable: Throwable ->
                Log.e("Repository.logout()", "Logout error: " + throwable.localizedMessage)
            }
    }

    fun deauthenticate(resetDataListener: OnResetDataListener?) {
        if (isAuthenticated()) {
            clear(resetDataListener)
            deletePushToken()
            val intent =
                Intent(context.getString(R.string.meetingdoctors_local_broadcast_deauthenticate))
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

    //endregion
    //region ONLINE DOCTORS
    private fun getDoctors(): ArrayList<Doctor> {
        if (doctors.size == 0) {
            val cachedDoctors = getDoctors(context)
            if (cachedDoctors != null) {
                doctors.addAll(cachedDoctors)
            }
        }
        return doctors
    }

    fun setDoctors(doctors: List<Doctor>) {
        this.doctors.clear()
        this.doctors.addAll(doctors)
        putDoctors(context, doctors)
    }

    fun getFilteredAndSortedDoctors(includeFilter: Int, excludeFilter: Int): ArrayList<Doctor> {
        val includedDoctors = ArrayList<Doctor>()
        if (includeFilter == 0) {
            includedDoctors.addAll(getDoctors())
        } else {
            for (doctor in getDoctors()) {
                if (doctor.speciality == null || doctor.speciality?.id == null) continue
                if (includeFilter and 1 != 0 && doctor.speciality?.id == Speciality.GENERAL_MEDICINE.value() ||
                    includeFilter and 2 != 0 && doctor.speciality?.id == Speciality.PEDIATRICS.value() ||
                    includeFilter and 4 != 0 && doctor.speciality?.id == Speciality.PSYCHOLOGY.value() ||
                    includeFilter and 8 != 0 && doctor.speciality?.id == Speciality.SPORTS_MEDICINE.value() ||
                    includeFilter and 16 != 0 && doctor.speciality?.id == Speciality.CUSTOMER_CARE.value() ||
                    includeFilter and 32 != 0 && doctor.speciality?.id == Speciality.MEDICAL_SUPPORT.value() ||
                    includeFilter and 64 != 0 && doctor.speciality?.id == Speciality.PERSONAL_TRAINING.value() ||
                    includeFilter and 128 != 0 && doctor.speciality?.id == Speciality.COMMERCIAL.value() ||
                    includeFilter and 256 != 0 && doctor.speciality?.id == Speciality.MEDICAL_APPOINTMENT.value() ||
                    includeFilter and 512 != 0 && doctor.speciality?.id == Speciality.CARDIOLOGY.value() ||
                    includeFilter and 1024 != 0 && doctor.speciality?.id == Speciality.GYNECOLOGY.value() ||
                    includeFilter and 2048 != 0 && doctor.speciality?.id == Speciality.PHARMACY.value() ||
                    includeFilter and 4096 != 0 && doctor.speciality?.id == Speciality.SEXOLOGY.value() ||
                    includeFilter and 8192 != 0 && doctor.speciality?.id == Speciality.NUTRITION.value() ||
                    includeFilter and 16384 != 0 && doctor.speciality?.id == Speciality.FERTILITY_CONSULTANT.value() ||
                    includeFilter and 32768 != 0 && doctor.speciality?.id == Speciality.NURSING.value() ||
                    includeFilter and 65536 != 0 && doctor.speciality?.id == Speciality.MEDICAL_ADVISOR.value() ||
                    includeFilter and 131072 != 0 && doctor.speciality?.id == Speciality.CUSTOMER_CARE_ISALUD.value() ||
                    (excludeFilter and 262144 != 0 && doctor.speciality?.id == Speciality.VETERINARY.value()) ||
                    (excludeFilter and 524288 != 0 && doctor.speciality?.id == Speciality.DOCTOR_GO_HEALTH_ADVISOR.value()) ||
                    (excludeFilter and 1048576 != 0 && doctor.speciality?.id == Speciality.FITNESS_COACHING.value()) ||
                    (excludeFilter and 2097152 != 0 && doctor.speciality?.id == Speciality.NUTRITIONAL_COACHING.value())
                ) {
                    includedDoctors.add(doctor)
                }
            }
        }
        val excludedDoctors = ArrayList<Doctor>()
        if (excludeFilter == 0) {
            excludedDoctors.addAll(includedDoctors)
        } else {
            for (doctor in includedDoctors) {
                if (doctor.speciality == null || doctor.speciality?.id == null) continue
                if (!(excludeFilter and 1 != 0 && doctor.speciality?.id == Speciality.GENERAL_MEDICINE.value()) &&
                    !(excludeFilter and 2 != 0 && doctor.speciality?.id == Speciality.PEDIATRICS.value()) &&
                    !(excludeFilter and 4 != 0 && doctor.speciality?.id == Speciality.PSYCHOLOGY.value()) &&
                    !(excludeFilter and 8 != 0 && doctor.speciality?.id == Speciality.SPORTS_MEDICINE.value()) &&
                    !(excludeFilter and 16 != 0 && doctor.speciality?.id == Speciality.CUSTOMER_CARE.value()) &&
                    !(excludeFilter and 32 != 0 && doctor.speciality?.id == Speciality.MEDICAL_SUPPORT.value()) &&
                    !(excludeFilter and 64 != 0 && doctor.speciality?.id == Speciality.PERSONAL_TRAINING.value()) &&
                    !(excludeFilter and 128 != 0 && doctor.speciality?.id == Speciality.COMMERCIAL.value()) &&
                    !(excludeFilter and 256 != 0 && doctor.speciality?.id == Speciality.MEDICAL_APPOINTMENT.value()) &&
                    !(excludeFilter and 512 != 0 && doctor.speciality?.id == Speciality.CARDIOLOGY.value()) &&
                    !(excludeFilter and 1024 != 0 && doctor.speciality?.id == Speciality.GYNECOLOGY.value()) &&
                    !(excludeFilter and 2048 != 0 && doctor.speciality?.id == Speciality.PHARMACY.value()) &&
                    !(excludeFilter and 4096 != 0 && doctor.speciality?.id == Speciality.SEXOLOGY.value()) &&
                    !(excludeFilter and 8192 != 0 && doctor.speciality?.id == Speciality.NUTRITION.value()) &&
                    !(excludeFilter and 16384 != 0 && doctor.speciality?.id == Speciality.FERTILITY_CONSULTANT.value()) &&
                    !(excludeFilter and 32768 != 0 && doctor.speciality?.id == Speciality.NURSING.value()) &&
                    !(excludeFilter and 65536 != 0 && doctor.speciality?.id == Speciality.MEDICAL_ADVISOR.value()) &&
                    !(excludeFilter and 131072 != 0 && doctor.speciality?.id == Speciality.CUSTOMER_CARE_ISALUD.value()) &&
                    !(excludeFilter and 262144 != 0 && doctor.speciality?.id == Speciality.VETERINARY.value()) &&
                    !(excludeFilter and 524288 != 0 && doctor.speciality?.id == Speciality.DOCTOR_GO_HEALTH_ADVISOR.value()) &&
                    !(excludeFilter and 1048576 != 0 && doctor.speciality?.id == Speciality.FITNESS_COACHING.value()) &&
                    !(excludeFilter and 2097152 != 0 && doctor.speciality?.id == Speciality.NUTRITIONAL_COACHING.value())
                ) {
                    excludedDoctors.add(doctor)
                }
            }
        }
        return excludedDoctors
    }

    fun getPendingMessagesCount(): Long {
        var pendingMessages: Long = 0
        if (getDoctors().size > 0) {
            synchronized(getDoctors()) {
                for (doctor in getDoctors()) {
                    pendingMessages += doctor.getPendingMessageCount().toLong()
                }
            }
        }
        return pendingMessages
    }

    fun getDoctorByUserHash(doctorUuserHash: String): Doctor? {
        synchronized(getDoctors()) {
            for (doctor in getDoctors()) {
                if (doctor.hash == doctorUuserHash) {
                    return doctor
                }
            }
        }
        return null
    }

    fun getDoctorByRoomId(roomId: Int): Doctor? {
        synchronized(getDoctors()) {
            for (doctor in getDoctors()) {
                if (doctor.room != null && doctor.room?.id == roomId) {
                    return doctor
                }
            }
        }
        return null
    }

    fun getDoctorBySpeciality(speciality: String?, offline: Boolean): Doctor? {
        val specialityId = id(speciality)
        return if (specialityId > 0) {
            getDoctorBySpeciality(specialityId, offline)
        } else null
    }

    fun getDoctorBySpecialityId(specialityId: Int, offline: Boolean): Doctor? {
        return if (specialityId > 0) {
            getDoctorBySpeciality(specialityId, offline)
        } else null
    }

    private fun getDoctorBySpeciality(specialityId: Int, offline: Boolean): Doctor? {
        val doctors: MutableList<Doctor> = ArrayList()
        synchronized(getDoctors()) { doctors.addAll(getDoctors()) }
        var firstDoctor: Doctor? = null
        for (doctor in doctors) {
            if (doctor.speciality?.id != null && doctor.speciality?.id == specialityId &&
                (offline || doctor.status == DOCTOR_STATUS_ONLINE)
            ) {
                if (firstDoctor == null) firstDoctor = doctor
                if (!doctor.isSaturated()) return doctor
            }
        }
        return firstDoctor
    }

    fun getDoctorById(professionals: List<Long>, offline: Boolean): Doctor? {
        val doctors: MutableList<Doctor> = ArrayList()
        synchronized(getDoctors()) { doctors.addAll(getDoctors()) }
        for (doctor in doctors) {
            for (professional in professionals) {
                if (doctor.id == professional &&
                    (offline || doctor.status == DOCTOR_STATUS_ONLINE)
                ) return doctor
            }
        }
        return null
    }

    //endregion
    //region PUSH
    fun getPushToken(): String? {
        return getString(PUSH_TOKEN_KEY)
    }

    fun setPushToken(pushToken: String) {
        if (getPushToken() == null || getPushToken() != pushToken) {
            put(PUSH_TOKEN_KEY, pushToken)
            registerPushToken()
        }
    }

    fun clearPushToken() {
        unregisterPushToken()
        clear(PUSH_TOKEN_KEY)
    }

    @SuppressLint("CheckResult", "HardwareIds")
    fun registerPushTokenFromNewToken(token: String) {
        Log.i("FCM", "registerPushTokenFromNewToken()")
        setPushToken(token)
    }

    @SuppressLint("CheckResult", "HardwareIds")
    private fun registerPushToken() {
        Log.i("FCM", "registerPushToken()")
        if (instance?.getUserData() != null) {
                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful && task.result != null) {
                            val token = task.result?.toString()
                            Log.d("FCM", "FirebaseInstanceId.getInstanceId() token[$token]")
                            token?.let { it1 -> Repository.instance?.setPushToken(it1) }
                            notificationsApi.registerPushToken(
                                "Bearer " + instance!!.getUserData()!!.jwt,
                                instance!!.getPushToken()!!,
                                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                            ).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    Log.i(
                                        "FCM",
                                        "registerPushToken() Registered successfully [" + getPushToken() + "]"
                                    )
                                }) {
                                    Log.i("FCM", "registerPushToken() Error [" + getPushToken() + "]")
                                }
                        } else {
                            Log.d("FCM", "FirebaseInstanceId.getInstanceId() error")
                        }
                    }
        }
    }

    @SuppressLint("CheckResult", "HardwareIds")
    private fun deletePushToken() {
        Log.i("FCM", "deletePushToken()")
        if (instance?.getUserData() != null) {
            val pushToken = getPushToken()
            if (pushToken != null) {
                notificationsApi.deletePushToken(
                    "Bearer " + instance!!.getUserData()!!.jwt,
                    pushToken
                ).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.i("FCM", "deletePushToken() delete successfully [$pushToken]")
                    }) {
                        Log.i("FCM", "deletePushToken() Error [$pushToken]")
                    }
            }
        }
    }

    private fun unregisterPushToken() {
        Log.i("Repository", "unregisterPushToken()")
        MDFirebaseMessagingService.unregisterToken()
    }

    //endregion
    //region CONSULTATIONS
    @SuppressLint("CheckResult")
    fun storeNpsRequest(
        rateResult: String,
        description: String?,
        id: Int,
        actionListener: NpsRatingDialogActions
    ) {
        val body = NpsBody(rateResult, description)
        consultationsRepository.storeNpsRequest(
            "Bearer " + instance?.getUserData()?.jwt,
            body
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.i("Nps", "storeNpsRequest() was successfully done!")
                actionListener.submitPoll()
            }) { throwable ->
                Log.e(
                    "Nps",
                    "storeNpsRequest() error :" + throwable.localizedMessage
                )
            }
    }

    //endregion
    fun getPrescription(): Single<Prescription> {
        return prescriptionRepository.getPrescription().map { (lastModifiedAt, url) ->
            Prescription(
                lastModifiedAt.formatToPrescriptionDate(),
                lastModifiedAt.formatToPrescriptionHour(),
                url
            )
        }.onErrorResumeNext { throwable: Throwable? ->
            if (throwable is HttpException) {
                val errorCode = throwable.code()
                if (errorCode == 404) {
                    Single.error<Prescription> { PrescriptionException.PrescriptionNotFoundException() }
                } else {
                    Single.error<Prescription>(throwable)
                }
            } else {
                Single.error<Prescription>(throwable)
            }
        }
    }


    fun downloadAndStoreFile(
        documentUrl: String,
        fileName: String,
        fileMimeType: FileMimeType
    ): Completable {
        return fileRepository.getFile(documentUrl).flatMapCompletable { value: ResponseBody ->
            fileRepository.writeResponseBodyToDisk(value, fileName, fileMimeType)
        }
    }

    interface GetUnreadMessageCountResponseListener {
        fun onResponse(count: Long)
        fun onError(e: Exception?)
    }

    @SuppressLint("CheckResult")
    fun getUnreadMessageCount(responseListener: GetUnreadMessageCountResponseListener) {
        Log.i("Repository", "getUnreadMessageCount()")
        if (isAuthenticated()) {
            instance?.getUserHash()?.let {
                chatApi.getUnreadMessageCount(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ getUnreadMessageCountResponse ->
                        responseListener.onResponse(getUnreadMessageCountResponse.count)
                    })
                    {
                        responseListener.onError(Exception("Network error"))
                    }
            }
        } else {
            responseListener.onError(Exception("No user authenticated"))
        }
    }

    interface GetSetupResponseListener {
        fun onResponse(error: Throwable?, setup: Setup?)
    }

    fun hasAccessToProfessional(doctor: Doctor): Boolean {
        return getUserData()?.status == Constants.USER_STATUS_ACTIVE.toLong()
                || doctor.app_role?.id != DOCTOR_ROLE_DOCTOR
                && doctor.app_role?.id != DOCTOR_ROLE_MEDICAL_SUPPORT
    }
}
