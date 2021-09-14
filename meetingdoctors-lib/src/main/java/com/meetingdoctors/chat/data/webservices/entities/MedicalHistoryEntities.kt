package com.meetingdoctors.chat.data.webservices.entities

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.meetingdoctors.chat.domain.entities.Company
import com.meetingdoctors.chat.domain.entities.Professional
import com.meetingdoctors.chat.domain.entities.Report
import com.squareup.moshi.Json


@Keep
internal data class GetAllergiesResponse(
        @SerializedName("data")
        val data: List<ExternalAllergy>,
        @SerializedName("total")
        val total: Int?,
        @SerializedName("per_page")
        val per_page: Int?,
        @SerializedName("current_page")
        val current_page: Int?,
        @SerializedName("last_page")
        val last_page: Int?,
        @SerializedName("from")
        val from: Int?,
        @SerializedName("to")
        val to: Int?,
        @SerializedName("next_page_url")
        val next_page_url: String?,
        @SerializedName("prev_page_url")
        val prev_page_url: String?)

@Keep
internal data class PostAllergyResponse(
        @SerializedName("data")
        val data: ExternalAllergy)

@Keep
internal data class GetDiseasesResponse(
        @SerializedName("data")
        val data: List<ExternalDisease>,
        @SerializedName("total")
        val total: Int,
        @SerializedName("per_page")
        val per_page: Int,
        @SerializedName("current_page")
        val current_page: Int,
        @SerializedName("last_page")
        val last_page: Int,
        @SerializedName("from")
        val from: Int?,
        @SerializedName("to")
        val to: Int?,
        @SerializedName("next_page_url")
        val next_page_url: String?,
        @SerializedName("prev_page_url")
        val prev_page_url: String?)

@Keep
internal data class PostDiseaseResponse(
        @SerializedName("item")
        val item: ExternalDisease)

@Keep
internal data class GetMedicationsResponse(
        @SerializedName("data")
        val data: List<ExternalMedication>,
        @SerializedName("total")
        val total: Int,
        @SerializedName("per_page")
        val per_page: Int,
        @SerializedName("current_page")
        val current_page: Int,
        @SerializedName("last_page")
        val last_page: Int,
        @SerializedName("from")
        val from: Int?,
        @SerializedName("to")
        val to: Int?,
        @SerializedName("next_page_url")
        val next_page_url: String?,
        @SerializedName("prev_page_url")
        val prev_page_url: String?)

@Keep
internal data class PostMedicationResponse(
        @SerializedName("item")
        val item: ExternalMedication)

@Keep
internal data class ExternalAllergy(
        @SerializedName("id")
        val id: Long,
        @SerializedName("name")
        var name: String,
        @SerializedName("severity")
        var severity: Long,
        @SerializedName("description")
        var description: String?,
        @SerializedName("customer_hash")
        var customerHash: String?,
        @SerializedName("professional_hash")
        var professionalHash: String? = null,
        @SerializedName("created_at")
        var createdAt: String? = null,
        @SerializedName("updated_at")
        var updatedAt: String? = null,
        @SerializedName("deleted_at")
        var deletedAt: String? = null)

@Keep
internal data class ExternalDisease(
        @SerializedName("id")
        val id: Long,
        @SerializedName("name")
        var name: String,
        @SerializedName("description")
        var description: String?,
        @SerializedName("diagnosis_date")
        var diagnosis_date: String?,
        @SerializedName("resolution_date")
        var resolution_date: String?,
        @SerializedName("customer_hash")
        var customerHash: String? = null,
        @SerializedName("created_at")
        var createdAt: String? = null,
        @SerializedName("updated_At")
        var updated_at: String? = null)

@Keep
internal data class ExternalMedication(
        @SerializedName("id")
        val id: Long,
        @SerializedName("name")
        var name: String,
        @SerializedName("posology")
        var posology: String?,
        @SerializedName("description")
        var description: String?,
        @SerializedName("customer_hash")
        var customerHash: String? = null)

@Keep
data class GetReportsResponse(
        @SerializedName("data")
        val data: List<Report>)
@Keep
data class ExternalReferral(
        @SerializedName("id")
        val id: Int?,
        @SerializedName("type")
        val type: String?,
        @SerializedName("payload")
        val payload: String?,
        @SerializedName("company_api_key")
        @Json(name = "company_api_key") @field:Json(name = "company_api_key")
        val companyApiKey: String?,
        @Json(name = "customer_hash") @field:Json(name = "customer_hash")
        @SerializedName("customer_hash")
        val customerHash: String?,
        @SerializedName("professional_hash")
        @Json(name = "professional_hash") @field:Json(name = "professional_hash")
        val professionalHash: String?,
        @SerializedName("filename")
        val filename: String?,
        @SerializedName("friendly_name")
        @Json(name = "friendly_name") @field:Json(name = "friendly_name")
        val friendlyName: String?,
        @SerializedName("url")
        val url: String?,
        @SerializedName("company")
        val company: Company?,
        @SerializedName("professional")
        val professional: Professional?,
        @SerializedName("created_at")
        @Json(name = "created_at") @field:Json(name = "created_at")
        val createdAt: String?) : RemoteModel

@Keep
data class GetReferralsResponse(
        @SerializedName("data")
        val data: List<ExternalReferral>) : RemoteModel

internal interface RemoteModel