package com.meetingdoctors.chat.data.webservices.endpoints

import com.meetingdoctors.chat.data.webservices.entities.*
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

internal interface MedicalHistoryApi {

    //region Allergies

    @GET("allergies")
    @Headers("No-sessionToken: true")
    fun getAllergies(@Header("Authorization") authorization: String)
            : Single<GetAllergiesResponse>

    @POST("allergies")
    @Headers("No-sessionToken: true")
    fun postAllergy(
            @Header("Authorization") authorization: String,
            @Query("name") name: String?,
            @Query( "severity") severity: String?,
            @Query("description") description: String?)
            : Single<PostAllergyResponse>

    @PUT("allergies/{id}")
    @Headers("No-sessionToken: true")
    fun putAllergy(
            @Header("Authorization") authorization: String,
            @Path("id") id: Long,
            @Body allergy: ExternalAllergy)
            : Single<PostAllergyResponse>

    @DELETE("allergies/{id}")
    @Headers("No-sessionToken: true")
    fun deleteAllergy(
            @Header("Authorization") authorization: String,
            @Path("id") id: Long)
            : Completable

    //endregion

    //region Diseases

    @GET("diseases")
    @Headers("No-sessionToken: true")
    fun getDiseases(@Header("Authorization") authorization: String)
            : Single<GetDiseasesResponse>

    @POST("diseases")
    @Headers("No-sessionToken: true")
    fun postDisease(
            @Header("Authorization") authorization: String,
            @Body disease: ExternalDisease)
            : Single<PostDiseaseResponse>

    @PUT("diseases/{id}")
    @Headers("No-sessionToken: true")
    fun putDisease(@Header("Authorization") authorization: String,
                   @Path("id") id: Long,
                   @Body disease: ExternalDisease)
            : Single<PostDiseaseResponse>

    @DELETE("diseases/{id}")
    @Headers("No-sessionToken: true")
    fun deleteDisease(@Header("Authorization") authorization: String,
                      @Path("id") id: Long)
            : Completable

    //endregion

    //region Medications

    @GET("medications")
    @Headers("No-sessionToken: true")
    fun getMedications(@Header("Authorization") authorization: String,
                       @Query("customer_hash") patientHash: String,
                       @Query("sort") sort: String? = null,
                       @Query("order") order: String? = null,
                       @Query("limit") limit: Int? = null)
            : Single<GetMedicationsResponse>

    @POST("medications")
    @Headers("No-sessionToken: true")
    fun postMedication(@Header("Authorization") authorization: String,
            @Body medication: ExternalMedication)
            : Single<PostMedicationResponse>

    @PUT("medications/{id}")
    @Headers("No-sessionToken: true")
    fun putMedication(@Header("Authorization") authorization: String,
                      @Path("id") id: Long,
                      @Body medication: ExternalMedication)
            : Single<PostMedicationResponse>

    @DELETE("medications/{id}")
    @Headers("No-sessionToken: true")
    fun deleteMedication(@Header("Authorization") authorization: String,
                      @Path("id") id: Long)
            : Completable

    //endregion

    //region Reports
    @GET
    @Headers("No-sessionToken: true")
    fun getReports(@Url url: String,
            @Header("Authorization") authorization: String)
            : Single<GetReportsResponse>

    //endregion

    //region Referrals
    @GET("/customers/v1/medical-derivations/")
    @Headers("No-sessionToken: true")
    fun getReferrals(@Header("Authorization") authorization: String)
            : Single<GetReferralsResponse>
}
