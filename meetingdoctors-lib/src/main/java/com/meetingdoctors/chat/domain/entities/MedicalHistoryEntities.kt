package com.meetingdoctors.chat.domain.entities

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

data class Allergy(
        @SerializedName("id")
        val id: Long,
        @SerializedName("name")
        var name: String,
        @SerializedName("severity")
        var severity: Long,
        @SerializedName("description")
        var details: String?)

data class Disease(val id: Long,
                            var name: String,
                            var details: String,
                            var diagnosisDate: String?,
                            var resolutionDate: String?) // TODO: Consider using java.util.Date

data class Medication(val id: Long,
                               var name: String,
                               var posology: String,
                               var details: String)

@Keep
data class Report(
        @SerializedName("id")
        val id: Long,
        @SerializedName("pdf_url")
        var pdf_url: String,
        @SerializedName("maker")
        var maker: Maker,
        @SerializedName("created_at")
        var created_at: String,
        @SerializedName("updated_at")
        var updated_at: String)

@Keep
data class Maker(
        @SerializedName("hash")
        var hash: String,
        @SerializedName("name")
        var name: String,
        @SerializedName("avatar")
        var avatar: String?,
        @SerializedName("description")
        var description: String?)

internal data class Referral(val id: Int?,
                             val type: ReferralType?,
                             val payload: String? = null,
                             val companyApiKey: String? = null,
                             val customerHash: String? = null,
                             val professionalHash: String? = null,
                             val filename: String?,
                             val friendlyName: String?,
                             val url: String?,
                             val company: Company? = null,
                             val professional: Professional?,
                             val createdAt: String?) : DomainModel

data class Company(
        @SerializedName("id")
        val id: Int?,
        @SerializedName("name")
        val name: String?,
        @SerializedName("logo")
        val logo: Any?
)

@Keep
data class Professional(
        @SerializedName("id")
        val id: Int?,
        @SerializedName("token")
        val token: String?,
        @SerializedName("connected")
        val connected: Int?,
        @SerializedName("hash")
        val hash: String?,
        @SerializedName("name")
        val name: String?
)

internal enum class ReferralType {
    interconsultation, diagnostic_procedures, therapeutic_procedures, undefined
}


internal interface DomainModel