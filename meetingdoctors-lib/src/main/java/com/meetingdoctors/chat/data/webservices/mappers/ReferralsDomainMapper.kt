package com.meetingdoctors.chat.data.webservices.mappers

import com.meetingdoctors.chat.domain.entities.ReferralType
import com.meetingdoctors.chat.domain.entities.Referral

internal object ReferralsDomainMapper: AbstractDomainModelMapper<com.meetingdoctors.chat.data.webservices.entities.ExternalReferral, Referral>() {
    override fun mapFromRemote(from: com.meetingdoctors.chat.data.webservices.entities.ExternalReferral): Referral =
        Referral(id = from.id,
                filename = from.filename,
                friendlyName = from.friendlyName,
                type= parseReferralType(from.type),
                url = from.url,
                customerHash = from.customerHash,
                professional = from.professional,
                professionalHash = from.professionalHash,
                company = from.company,
                createdAt = from.createdAt
        )

    private fun parseReferralType(type: String?) : ReferralType {
        return when (type) {
            "interconsultation" -> ReferralType.interconsultation
            "diagnostic_procedures" -> ReferralType.diagnostic_procedures
            "therapeutic_procedures" -> ReferralType.therapeutic_procedures
            else -> ReferralType.undefined
        }
    }
}