package com.meetingdoctors.chat.data.webservices.endpoints

import com.meetingdoctors.chat.data.webservices.ApiReponse
import com.meetingdoctors.chat.data.webservices.entities.PrescriptionResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

internal interface PrescriptionApi {

    @GET("document")
    @Headers("No-sessionToken: true", "Content-Type: application/json", "Accept: application/json")
    fun getPrescription(@Header("Authorization") authorization: String)
            : Single<ApiReponse<PrescriptionResponse>>
}