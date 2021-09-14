package com.meetingdoctors.chat.domain.repositories

import com.meetingdoctors.chat.data.webservices.entities.GetReferralsResponse
import com.meetingdoctors.chat.data.webservices.entities.GetReportsResponse
import com.meetingdoctors.chat.domain.entities.Allergy
import com.meetingdoctors.chat.domain.entities.Disease
import com.meetingdoctors.chat.domain.entities.Medication
import io.reactivex.Completable
import io.reactivex.Single

interface MedicalHistoryRepository {

    //region Allergies

    fun getAllergies(): Single<List<Allergy>>
    fun getAllergyById(id: Long): Single<Allergy>
    fun postAllergy(allergy: Allergy): Single<Allergy>
    fun putAllergy(allergy: Allergy): Single<Allergy>
    fun deleteAllergy(id: Long): Completable

    //endregion

    //region Diseases

    fun getDiseases(): Single<List<Disease>>
    fun getDiseaseById(id: Long): Single<Disease>
    fun postDisease(disease: Disease): Single<Disease>
    fun putDisease(disease: Disease): Single<Disease>
    fun deleteDisease(id: Long): Completable

    //endregion

    //region Medications

    fun getMedications(): Single<List<Medication>>
    fun getMedicationById(id: Long): Single<Medication>
    fun postMedication(medication: Medication): Single<Medication>
    fun putMedication(medication: Medication): Single<Medication>
    fun deleteMedication(id: Long): Completable

    //endregion

    //region Reports

    fun getReports(): Single<GetReportsResponse>

    //endregion

    //region Documents

    fun getDocuments(): Single<GetReferralsResponse>

    //endregion
}