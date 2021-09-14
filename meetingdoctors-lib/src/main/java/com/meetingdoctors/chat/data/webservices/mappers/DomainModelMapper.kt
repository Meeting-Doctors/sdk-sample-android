package com.meetingdoctors.chat.data.webservices.mappers

import com.meetingdoctors.chat.data.webservices.entities.RemoteModel
import com.meetingdoctors.chat.domain.entities.DomainModel

/**
 *
 * Interface for [RemoteModel] to [DomainModel] mappers.
 * It transforms API remote models into domain data models
 *
 * @param <RM> the remote model input type
 * @param <DM> the entity model output type
 */
internal interface DomainModelMapper<in RM : RemoteModel, out DM : DomainModel> {
    fun mapFromRemote(from: RM): DM
    fun mapFromRemoteArray(array: List<RM>): List<DM>
}