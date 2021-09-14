package com.meetingdoctors.chat.data.webservices.toolkit

import com.meetingdoctors.chat.BuildConfig
import com.meetingdoctors.chat.data.Constants.CONNECTION_TIMEOUT
import com.meetingdoctors.chat.data.Constants.MAX_CACHE_SIZE_IN_BYTES
import com.meetingdoctors.chat.data.Constants.READ_TIMEOUT
import com.meetingdoctors.chat.data.Constants.WRITE_TIMEOUT
import com.meetingdoctors.chat.data.Repository
import okhttp3.Cache
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

class OkHttpClientFactory(private val repository: Repository) {

    fun createOkHttpClient() = defaultOkHttpClient

    private val defaultOkHttpClient: OkHttpClient by lazy {
        OkHttpClient().newBuilder()
                // Timeouts
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)

                // Add certificate pinner
                .certificatePinner(CertificatePinner.Builder()
                        .add("*.medipremium.com", BuildConfig.CA_MEDIPREMIUM)
                        .add("*.meetingdoctors.com", BuildConfig.CA_MD_1)
                        .add("*.dev.meetingdoctors.com", BuildConfig.CA_MD_1_DEV)
                        .add("*.staging.meetingdoctors.com", BuildConfig.CA_MD_1_STAGING)
                        .add("*.meetingdoctors.com", BuildConfig.CA_MD_2)
                        .add("*.dev.meetingdoctors.com", BuildConfig.CA_MD_2_DEV)
                        .add("*.staging.meetingdoctors.com", BuildConfig.CA_MD_2_STAGING)
                        .add("*.meetingdoctors.com", BuildConfig.CA_MD_3)
                        .add("*.dev.meetingdoctors.com", BuildConfig.CA_MD_3_DEV)
                        .add("*.staging.meetingdoctors.com", BuildConfig.CA_MD_3_STAGING)
                        .add("*.dev.meetingdoctors.com", BuildConfig.CA_MD_4_DEV)
                        .build())

                // Add cache
                .cache(Cache(File(repository.getCacheDir(), "okhttp"), MAX_CACHE_SIZE_IN_BYTES))

                // Add logging interceptor
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                })

                .addInterceptor { chain ->
                    val request = chain.request()
                    val builder = request.newBuilder()

                    if (request.header("No-apiKey") == null) {
                        repository.getApiKey()?.let { builder.addHeader("apiKey", it) }
                    } else {
                        builder.removeHeader("No-apiKey")
                    }

                    if (request.header("Authorization") != null) {
                        builder.header("Authorization", "Bearer " + repository.getUserData()?.jwt)
                    }

                    if (request.header("No-sessionToken") == null) {
                        repository.getSessionToken()?.let { builder.header("sessionToken", it) }
                    } else {
                        builder.removeHeader("No-sessionToken")
                    }

                    chain.proceed(builder.build())
                }
                .build()
    }

}