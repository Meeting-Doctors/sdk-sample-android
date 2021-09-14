package com.meetingdoctors.chat.data.webservices

import androidx.annotation.Keep

@Keep
class ApiReponse<T>() {
    var data: T? = null
}