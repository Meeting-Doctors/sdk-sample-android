@file:JvmName("NpsEntities")
package com.meetingdoctors.chat.data.webservices.entities

import com.google.gson.annotations.SerializedName

internal data class NpsBody(
        @SerializedName("value")
        val value: String,
        @SerializedName("description")
        val description: String?)