package com.meetingdoctors.chat.data.webservices.endpoints

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.POST
import retrofit2.http.Query

internal interface RequestMessageApi {
    @POST("actions/requestMessage")
    fun requestMessage(@Query("message") message: String,
                       @Query("professional_hash") professionalHash: String)
            : Single<ResponseBody>
}