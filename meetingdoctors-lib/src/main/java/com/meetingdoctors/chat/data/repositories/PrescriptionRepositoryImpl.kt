package com.meetingdoctors.chat.data.repositories

import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.data.webservices.BEARER_PREFIX
import com.meetingdoctors.chat.data.webservices.endpoints.PrescriptionApi
import com.meetingdoctors.chat.data.webservices.entities.PrescriptionResponse
import com.meetingdoctors.chat.domain.repositories.PrescriptionRepository
import io.reactivex.Single

internal class PrescriptionRepositoryImpl(private val prescriptionApi: PrescriptionApi,
                                          private val repository: Repository) : PrescriptionRepository {

    override fun getPrescription(): Single<PrescriptionResponse> {
        return prescriptionApi.getPrescription(
                BEARER_PREFIX + repository.getUserData()?.jwt
        ).map {
            it.data
        }
    }
}