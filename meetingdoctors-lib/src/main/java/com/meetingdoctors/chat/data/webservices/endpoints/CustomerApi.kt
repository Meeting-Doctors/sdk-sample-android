package com.meetingdoctors.chat.data.webservices.endpoints

import com.meetingdoctors.chat.data.webservices.entities.*
import com.meetingdoctors.chat.data.webservices.entities.CheckVerificationCodeResponse
import com.meetingdoctors.chat.data.webservices.entities.UserDataResponse
import com.meetingdoctors.chat.domain.entities.Setup
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

internal interface CustomerApi {

    @GET("2/authenticate")
    @Headers("No-sessionToken: true")
    fun authenticate(@Query("installationGuid") installationGuid: String,
                     @Query("userToken") userToken: String)
            : Single<CheckVerificationCodeResponse>

    @GET("2/setup")
    @Headers("No-sessionToken: true")
    fun getSetup(@Query("osName") osName: String,
                 @Query("osVersion") osVersion: String,
                 @Query("appVersion") appVersion: String,
                 @Query("installationGuid") installationGuid: String,
                 @Query("deviceModel") deviceModel: String,
                 @Query("deviceId") deviceId: String,
                 @Query("bundleId") bundleId: String,
                 @Query("languageCode") languageCode: String,
                 @Query("countryCode") countryCode: String?)
            : Single<Setup>

    @GET("2/userdata")
    fun getUserData(@Query("osName") osName: String?,
                    @Query("osVersion") osVersion: String?,
                    @Query("appVersion") appVersion: String?,
                    @Query("deviceModel") deviceModel: String?,
                    @Query("referrer") referrer: String?)
            : Single<UserDataResponse>

    @PUT("white-label/v1/profile")
    fun updateProfileInfo(@Body updateProfileInfoBody: UpdateProfileInfoBody)
            : Single<UserDataResponse>

    @POST("white-label/v1/relationships")
    fun sendRelationshipsInvitationCode(@Body sendInvitationCodeBody: SendInvitationCodeBody)
            : Single<SendInvitationCodeResponse>

    @POST("sdk/v3/logout")
    @Headers("No-sessionToken: true")
    fun logout(@Header("Authorization") authorization: String) : Completable
}