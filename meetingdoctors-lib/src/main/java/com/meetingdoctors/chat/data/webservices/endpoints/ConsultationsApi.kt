package com.meetingdoctors.chat.data.webservices.endpoints

import com.meetingdoctors.chat.data.webservices.entities.NpsBody
import io.reactivex.Completable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

internal interface ConsultationsApi {
    @POST("v1/nps")
    fun storeNps(@Header("Authorization") authorization: String,
                 @Body npsBody: NpsBody)
     : Completable
}