package com.meetingdoctors.chat.data.webservices.endpoints

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface FileApi {

    @Streaming
    @GET
    fun downloadFileWithDynamicUrl(@Url fileUrl: String): Single<Response<ResponseBody>>
}