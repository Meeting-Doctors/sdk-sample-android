package com.meetingdoctors.chat.net

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.meetingdoctors.chat.BuildConfig
import com.meetingdoctors.chat.data.Constants
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by Héctor Manrique on 4/14/21.
 */

class ServerInterface(context: Context) {
    private val TAG = this.javaClass.simpleName
    private lateinit var mOkHttpClient: OkHttpClient


    companion object {
        var mServerInterface: ServerInterface? = null
        fun getInstance(context: Context): ServerInterface? {
            if (mServerInterface == null) {
                mServerInterface = ServerInterface(context)
            }
            return mServerInterface
        }
    }

    init {
        try {
            mOkHttpClient = OkHttpClient.Builder()
                    .certificatePinner(CertificatePinner.Builder()
                            .add("*.medipremium.com", BuildConfig.CA_MEDIPREMIUM)
                            .add("*.meetingdoctors.com", BuildConfig.CA_MD_1)
                            .add("*.dev.meetingdoctors.com", BuildConfig.CA_MD_1_DEV)
                            .add("*.meetingdoctors.com", BuildConfig.CA_MD_2)
                            .add("*.dev.meetingdoctors.com", BuildConfig.CA_MD_2_DEV)
                            .add("*.meetingdoctors.com", BuildConfig.CA_MD_3)
                            .add("*.dev.meetingdoctors.com", BuildConfig.CA_MD_3_DEV)
                            .add("*.dev.meetingdoctors.com", BuildConfig.CA_MD_4_DEV)
                            .build())
                    .cache(Cache(File(context.cacheDir, "okhttp"), Constants.MAX_CACHE_SIZE_IN_BYTES))
                    .connectTimeout(Constants.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(Constants.READ_TIMEOUT, TimeUnit.MILLISECONDS)
                    .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                    .build()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface ResponseListener {
        fun onResponse(error: Throwable?, statusCode: Int, data: Any?)
    }

    private fun createBuilder(url: String,
                              headers: Map<String, String>?,
                              fromCache: Boolean = false): Request.Builder {

        val builder = Request.Builder()
        if (fromCache) {
            builder.cacheControl(CacheControl.Builder().onlyIfCached().build())
        }
        builder.url(url)
        if (headers != null) {
            for ((key, value) in headers) {
                builder.addHeader(key, value)
            }
        }
        return builder
    }

    @JvmOverloads
    operator fun get(url: String,
                     headers: Map<String, String>?,
                     responseListener: ResponseListener,
                     fromCache: Boolean = false,
                     fakeData: Any? = null): Call {
        Log.i(TAG, "get($url)")

        val builder = createBuilder(url, headers, fromCache)
        val request = builder.build()
        val call = mOkHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post { responseListener.onResponse(e, 0, null) }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                logServerInterfaceCallResponse(url, response,"get")
                processResponse(response, responseListener, fakeData)
            }
        })
        return call
    }

    @JvmOverloads
    fun postJson(
        url: String,
        json: String?,
        headers: Map<String, String>?,
        responseListener: ResponseListener,
        fakeData: Any? = null
    ): Call {
        Log.i(TAG, "postJson($url)")
        val parsedJson = "application/json; charset=utf-8".toMediaTypeOrNull()

        val builder = createBuilder(url, headers)
        val body = json?.let { RequestBody.create(parsedJson, it) }
        body?.let { builder.post(it) }
        val request = builder.build()
        val call = mOkHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post { responseListener.onResponse(e, 0, null) }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                logServerInterfaceCallResponse(url, response, "post")
                processResponse(response, responseListener, fakeData)
            }
        })
        return call
    }

    @JvmOverloads
    fun putJson(
        url: String, json: String?,
        headers: Map<String, String>?,
        responseListener: ResponseListener,
        fakeData: Any? = null
    ): Call {
        Log.i(TAG, "putJson($url)")
        val parsedJson = "application/json; charset=utf-8".toMediaTypeOrNull()
        val builder = createBuilder(url, headers)
        val body = json?.let { RequestBody.create(parsedJson, it) }
        body?.let { builder.put(it) }
        val request = builder.build()
        val call = mOkHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post { responseListener.onResponse(e, 0, null) }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                logServerInterfaceCallResponse(url, response, "put")
                processResponse(response, responseListener, fakeData)
            }
        })
        return call
    }

    @JvmOverloads
    fun delete(url: String,
               headers: Map<String, String>?,
               responseListener: ResponseListener,
               fromCache: Boolean = false,
               fakeData: Any? = null): Call {
        Log.i(TAG, "delete($url)")
        val builder = createBuilder(url, headers, fromCache)
        builder.delete()
        val request = builder.build()
        val call = mOkHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post { responseListener.onResponse(e, 0, null) }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                logServerInterfaceCallResponse(url, response,"get")
                processResponse(response, responseListener, fakeData)
            }
        })
        return call
    }

    private fun logServerInterfaceCallResponse(url: String, response: Response, methodType: String) {
        Log.i("ServerInterface",
                "$methodType($url) onResponse() statusCode[${response.code}] " +
                        "statusMessage[${response.message}] contentLength["
                        + (if (response.body != null) response.body!!.contentLength()
                else 0) + "]" + if (response.cacheResponse != null) " from cache" else "")
    }

    private fun processResponse(response: Response, responseListener: ResponseListener, fakeData: Any?) {
        try {
            if (!response.isSuccessful) {
                Handler(Looper.getMainLooper()).post {
                    responseListener.onResponse(Exception(response.message),
                            response.code,
                            null)
                }
            } else {
                if (response.body!!.contentType() != null) {
                    val contentType = response.body!!.contentType()!!.subtype
                    when (contentType.toLowerCase()) {
                        "json" -> {
                            val body = response.body!!.string()
                            Handler(Looper.getMainLooper()).post {
                                if (fakeData == null) {
                                    responseListener.onResponse(null, response.code, body)
                                } else {
                                    responseListener.onResponse(null, response.code, fakeData)
                                }
                            }
                        }
                        "jpg", "png" -> {
                            val bytes = response.body!!.bytes()
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            Handler(Looper.getMainLooper()).post {
                                if (fakeData == null) {
                                    responseListener.onResponse(null, response.code, bitmap)
                                } else {
                                    responseListener.onResponse(null, response.code, fakeData)
                                }
                            }
                        }
                    }
                } else {
                    responseListener.onResponse(null, response.code, response.body!!.string())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Handler(Looper.getMainLooper()).post { responseListener.onResponse(e, 0, null) }
        }
    }
}
