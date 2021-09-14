package com.meetingdoctors.chat.data.webservices.endpoints

import io.reactivex.Completable
import retrofit2.http.*

internal interface NotificationsApi {

    @POST("tokens?os=android")
    fun registerPushToken(
            @Header("Authorization") authorization: String,
            @Query("token") pushToken: String,
            @Query("device_id") deviceId: String)
            : Completable

    @DELETE("tokens/{pushToken}")
    fun deletePushToken(
            @Header("Authorization") authorization: String,
            @Path("pushToken") pushToken: String)
            : Completable
}