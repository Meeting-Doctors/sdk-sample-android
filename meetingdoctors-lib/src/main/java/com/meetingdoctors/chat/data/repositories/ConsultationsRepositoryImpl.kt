package com.meetingdoctors.chat.data.repositories

import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.data.webservices.endpoints.ConsultationsApi
import com.meetingdoctors.chat.data.webservices.entities.NpsBody
import com.meetingdoctors.chat.domain.repositories.ConsultationsRepository
import com.meetingdoctors.chat.helpers.NpsHelper
import io.reactivex.Completable

internal class ConsultationsRepositoryImpl(private val consultationsApi: ConsultationsApi,
                                           private val repository: Repository) : ConsultationsRepository {

    override fun storeNpsRequest(auth: String, body: NpsBody): Completable =
            consultationsApi.storeNps("Bearer ${Repository.instance?.getUserData()?.jwt}", body).doOnComplete {
                NpsHelper.storeNpsStatusCompleted(true)
            }
}