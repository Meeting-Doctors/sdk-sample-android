package com.meetingdoctors.chat.data.webservices.mappers

import com.meetingdoctors.chat.data.webservices.entities.RemoteModel
import com.meetingdoctors.chat.domain.entities.DomainModel

/**
 * Abstract [RemoteModel] to [DomainModel] mapper (from remote to domain layer).
 */
internal abstract class AbstractDomainModelMapper<in RM, out DM> : DomainModelMapper<RM, DM>
        where RM : RemoteModel, DM : DomainModel {

    abstract override fun mapFromRemote(from: RM): DM

    override fun mapFromRemoteArray(array: List<RM>): List<DM> =
            array.map { mapFromRemote(it) }

}