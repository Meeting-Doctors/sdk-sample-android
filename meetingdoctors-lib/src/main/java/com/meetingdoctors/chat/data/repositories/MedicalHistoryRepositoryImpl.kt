package com.meetingdoctors.chat.data.repositories

import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.data.webservices.BEARER_PREFIX
import com.meetingdoctors.chat.data.webservices.endpoints.MedicalHistoryApi
import com.meetingdoctors.chat.data.webservices.entities.GetReferralsResponse
import com.meetingdoctors.chat.data.webservices.entities.GetReportsResponse
import com.meetingdoctors.chat.data.webservices.getConsultationsServer
import com.meetingdoctors.chat.data.webservices.mappers.AllergyMapper
import com.meetingdoctors.chat.data.webservices.mappers.DiseaseMapper
import com.meetingdoctors.chat.data.webservices.mappers.MedicationMapper
import com.meetingdoctors.chat.domain.entities.Allergy
import com.meetingdoctors.chat.domain.entities.Disease
import com.meetingdoctors.chat.domain.entities.Medication
import com.meetingdoctors.chat.domain.repositories.MedicalHistoryRepository
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*
import kotlin.NoSuchElementException

internal class MedicalHistoryRepositoryImpl(private val medicalHistoryApi: MedicalHistoryApi,
                                            private val repository: Repository) : MedicalHistoryRepository {

    private val allergies: MutableList<Allergy> = ArrayList()
    private val diseases: MutableList<Disease> = ArrayList()
    private val medications: MutableList<Medication> = ArrayList()

    //region Allergies

    override fun getAllergies() = getAllergiesFullImpl()

    private fun getAllergiesFullImpl(limit: Int? = null, order: String? = null, sort: String? = null): Single<List<Allergy>> =
            medicalHistoryApi.getAllergies(
                    BEARER_PREFIX + repository.getUserData()?.jwt)
                    .map {
                        allergies.clear()
                        allergies.addAll(it.data.map { item -> AllergyMapper().transform(item) })
                        allergies
                    }

    override fun getAllergyById(id: Long): Single<Allergy> {
        val allergy: Allergy? = allergies.firstOrNull { it.id == id }
        return if (allergy != null) {
            Single.just(allergy)
        } else {
            Single.error(NoSuchElementException("No allergy with id $id."))
        }
    }

    override fun postAllergy(allergy: Allergy): Single<Allergy> =
            medicalHistoryApi.postAllergy(BEARER_PREFIX + repository.getUserData()?.jwt,
                    allergy.name, allergy.severity.toString(), allergy.details)
                    .map {
                        val postedAllergy = AllergyMapper().transform(it.data)
                        allergies.add(postedAllergy)
                        postedAllergy
                    }

    override fun putAllergy(allergy: Allergy): Single<Allergy> {
        return if (repository.getUserHash() != null) {
            medicalHistoryApi.putAllergy(
                    BEARER_PREFIX + repository.getUserData()?.jwt,
                    allergy.id,
                    AllergyMapper().transform(allergy, repository.getUserHash()!!))
                    .map { postedAllergy ->
                        val index = allergies.indexOfFirst { it.id == postedAllergy.data.id }
                        if (index != -1) {
                            val mappedAllergy = AllergyMapper().transform(postedAllergy.data)
                            allergies[index] = mappedAllergy
                            mappedAllergy
                        } else {
                            throw (NoSuchElementException("No allergy with id $allergy.item.id."))
                        }
                    }
        } else {
            Single.error(NoSuchElementException("Userhash is null."))
        }

    }

    override fun deleteAllergy(id: Long): Completable =
            medicalHistoryApi.deleteAllergy(BEARER_PREFIX + repository.getUserData()?.jwt, id)
                    .doOnComplete {
                        allergies.removeAll { it.id == id }
                    }

    //endregion

    //region Diseases

    override fun getDiseases(): Single<List<Disease>> = getDiseasesFullImpl()

    private fun getDiseasesFullImpl(limit: Int? = null, order: String? = null, sort: String? = null): Single<List<Disease>> =
            medicalHistoryApi.getDiseases(BEARER_PREFIX + repository.getUserData()?.jwt)
                    .map {
                        diseases.clear()
                        diseases.addAll(it.data.map { item -> DiseaseMapper().transform(item) })
                        diseases
                    }

    override fun getDiseaseById(id: Long): Single<Disease> {
        val disease: Disease? = diseases.firstOrNull { it.id == id }
        return if (disease != null) {
            Single.just(disease)
        } else {
            Single.error(NoSuchElementException("No disease with id $id."))
        }
    }

    override fun postDisease(disease: Disease): Single<Disease> {
        return if (repository.getUserHash() != null) {
            medicalHistoryApi.postDisease(BEARER_PREFIX + repository.getUserData()?.jwt,
                    DiseaseMapper().transform(disease, repository.getUserHash()!!))
                    .map {
                        val postedDisease = DiseaseMapper().transform(it.item)
                        diseases.add(postedDisease)
                        postedDisease
                    }
        } else {
            Single.error(NoSuchElementException("Userhash is null."))
        }
    }

    override fun putDisease(disease: Disease): Single<Disease> {
        return if (repository.getUserHash() != null) {
            medicalHistoryApi.putDisease(BEARER_PREFIX + repository.getUserData()?.jwt,
                    disease.id, DiseaseMapper().transform(disease, repository.getUserHash()!!))
                    .map { postedDisease ->
                        val index = diseases.indexOfFirst { it.id == postedDisease.item.id }
                        if (index != -1) {
                            val mappedDisease = DiseaseMapper().transform(postedDisease.item)
                            diseases[index] = mappedDisease
                            mappedDisease
                        } else {
                            throw (NoSuchElementException("No disease with id $disease.item.id."))
                        }
                    }
        } else {
            Single.error(NoSuchElementException("Userhash is null."))
        }
    }

    override fun deleteDisease(id: Long): Completable =
            medicalHistoryApi.deleteDisease(BEARER_PREFIX + repository.getUserData()?.jwt, id)
                    .doOnComplete {
                        diseases.removeAll { it.id == id }
                    }

    //endregion

    //region Medications

    override fun getMedications() = getMedicationsFullImpl()

    private fun getMedicationsFullImpl(limit: Int? = null,
                                       order: String? = null,
                                       sort: String? = null): Single<List<Medication>> {
        return if (repository.getUserHash() != null) {
            medicalHistoryApi.getMedications(BEARER_PREFIX + repository.getUserData()?.jwt,
                    patientHash = repository.getUserHash()!!,
                    limit = limit,
                    order = order,
                    sort = sort)
                    .map {
                        medications.clear()
                        medications.addAll(it.data.map { item -> MedicationMapper().transform(item) })
                        medications
                    }
        } else {
            Single.error(NoSuchElementException("Userhash is null."))
        }

    }

    override fun getMedicationById(id: Long): Single<Medication> {
        val medication: Medication? = medications.firstOrNull { it.id == id }
        return if (medication != null) {
            Single.just(medication)
        } else {
            Single.error(NoSuchElementException("No medication with id $id."))
        }
    }

    override fun postMedication(medication: Medication): Single<Medication> {

        return if (repository.getUserHash() != null) {
            medicalHistoryApi.postMedication(BEARER_PREFIX + repository.getUserData()?.jwt,
                    MedicationMapper().transform(medication, repository.getUserHash()!!))
                    .map {
                        val postedMedication = MedicationMapper().transform(it.item)
                        medications.add(postedMedication)
                        postedMedication
                    }
        } else {
            Single.error(NoSuchElementException("Userhash is null."))
        }
    }

    override fun putMedication(medication: Medication): Single<Medication> {

        return if (repository.getUserHash() != null) {
            medicalHistoryApi.putMedication(BEARER_PREFIX + repository.getUserData()?.jwt,
                    medication.id, MedicationMapper().transform(medication, repository.getUserHash()!!))
                    .map { postedMedication ->
                        val index = medications.indexOfFirst { it.id == postedMedication.item.id }
                        if (index != -1) {
                            val mappedMedication = MedicationMapper().transform(postedMedication.item)
                            medications[index] = mappedMedication
                            mappedMedication
                        } else {
                            throw (NoSuchElementException("No medication with id $medication.item.id."))
                        }
                    }
        } else {
            Single.error(NoSuchElementException("Userhash is null."))
        }
    }


    override fun deleteMedication(id: Long): Completable =
            medicalHistoryApi.deleteMedication(BEARER_PREFIX + repository.getUserData()?.jwt,
                    id)
                    .doOnComplete {
                        medications.removeAll { it.id == id }
                    }

    //endregion

    //region Reports

    override fun getReports(): Single<GetReportsResponse> =
            medicalHistoryApi.getReports("${getConsultationsServer(repository.getEnvironmentTarget())}v1/medical-history/reports",
                    BEARER_PREFIX + Repository.instance?.getUserData()?.jwt)

    override fun getDocuments(): Single<GetReferralsResponse> =
            medicalHistoryApi.getReferrals(BEARER_PREFIX + repository.getUserData()?.jwt)

    //endregion
}