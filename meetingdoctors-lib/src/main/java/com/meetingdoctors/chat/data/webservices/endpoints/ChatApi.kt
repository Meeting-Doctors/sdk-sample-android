package com.meetingdoctors.chat.data.webservices.endpoints

import com.meetingdoctors.chat.data.webservices.entities.GetUnreadMessageCountResponse
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

internal interface ChatApi {
    @GET("registerPushToken")
    fun registerPushToken(@Query("userHash") userHash: String,
                          @Query("token") token: String,
                          @Query("os") os: String,
                          @Query("deviceId") deviceId: String)
            : Completable

    @GET("unreadMessageCount")
    fun getUnreadMessageCount(@Query("userHash") userHash: String)
            : Single<GetUnreadMessageCountResponse>
}