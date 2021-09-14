package com.meetingdoctors.chat.data.webservices.entities

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.meetingdoctors.chat.domain.entities.UserData
import com.squareup.moshi.Json

@Keep
internal class CheckVerificationCodeResponse(
        val sessionToken: String,
        val userHash: String,
        val jwt: String
)

@Keep
internal data class UserDataResponse(
        @Json(name = "userData") @field:Json(name = "userData")
        val userData: UserData
)

@Keep
data class UpdateProfileInfoBody(
        @Json(name = "first_name") @field:Json(name = "first_name")
        val first_name: String,
        @Json(name = "last_name") @field:Json(name = "last_name")
        val last_name: String,
        @Json(name = "gender") @field:Json(name = "gender")
        val gender: Long,
        @Json(name = "birth_date") @field:Json(name = "birth_date")
        val birth_date: String)

@Keep
internal data class SendInvitationCodeResponse(
        @Json(name = "message") @field:Json(name = "message")
        val message: String
)

@Keep class SendInvitationCodeBody(
        @Json(name = "relationship_code") @field:Json(name = "relationship_code")
        val relationshipCode: String
)
