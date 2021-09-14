package com.meetingdoctors.chat.data.webservices.entities

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class PrescriptionResponse(
        @field:Json(name = "last_modified_at")
        val lastModifiedAt: String,
        @field:Json(name = "url")
        val url: String)