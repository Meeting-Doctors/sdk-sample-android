package com.meetingdoctors.chat.domain.entities

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class Setup(val minAppVersion: Long,
                          val paymentWithGoogle: Boolean,
                          val showSmsLogin: Boolean,
                          val welcomeMessageProfessionals: String?,
                          var referrer: String?,
                          val medicalHistory: MedicalHistory?
)

@Keep
data class MedicalHistory(@Json(name = "active") @field:Json(name = "active")
                                   val active: Boolean?,
                                   @Json(name = "options") @field:Json(name = "options")
                                   val options: Options?)

@Keep
data class Options(@Json(name = "hasMedicalDerivations") @field:Json(name = "hasMedicalDerivations")
                            val hasMedicalDerivations: Boolean,
                            @Json(name = "hasMedicalReports") @field:Json(name = "hasMedicalReports")
                            val hasMedicalReports: Boolean,
                            @Json(name = "hasPrescription") @field:Json(name = "hasPrescription")
                            val hasPrescription: Boolean)

@Keep
data class UserData(@Json(name = "status") @field:Json(name = "status")
                             val status: Long,
                             @Json(name = "firstName") @field:Json(name = "firstName")
                             val firstName: String?,
                             @Json(name = "lastName") @field:Json(name = "lastName")
                             val lastName: String?,
                             @Json(name = "dni") @field:Json(name = "dni")
                             val dni: String?,
                             @Json(name = "email") @field:Json(name = "email")
                             val email: String?,
                             @Json(name = "gender") @field:Json(name = "gender")
                             val gender: Long?,
                             @Json(name = "mobilePhone") @field:Json(name = "mobilePhone")
                             val mobilePhone: String?,
                             @Json(name = "companyName") @field:Json(name = "companyName")
                             val companyName: String?,
                             @Json(name = "coverageName") @field:Json(name = "coverageName")
                             val coverageName: String?,
                             @Json(name = "banned") @field:Json(name = "banned")
                             val banned: Long?,
                             @Json(name = "contractNumber") @field:Json(name = "contractNumber")
                             val contractNumber: String?,
                             @Json(name = "birthDate") @field:Json(name = "birthDate")
                             val birthDate: String?,
                             @Json(name = "cardNumber") @field:Json(name = "cardNumber")
                             val cardNumber: String?,
                             @Json(name = "description") @field:Json(name = "description")
                             val description: String?,
                             @Json(name = "jwt") @field:Json(name = "jwt")
                             val jwt: String?,
                             var features: Features? = null)

@Keep
class Features {
    var video_call: Boolean? = null
    var video_call_1to1: Boolean? = null
}
