package com.meetingdoctors.chat.domain.repositories

import com.meetingdoctors.chat.data.webservices.entities.NpsBody
import io.reactivex.Completable

internal interface ConsultationsRepository {
    fun storeNpsRequest(auth: String, body: NpsBody): Completable
}