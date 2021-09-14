package com.meetingdoctors.chat.data.webservices.mappers

import com.meetingdoctors.chat.data.webservices.entities.ExternalAllergy
import com.meetingdoctors.chat.domain.entities.Allergy

internal class AllergyMapper {

    fun transform(externalAllergy: ExternalAllergy): Allergy = externalAllergy.let {
        val (id, name, severity, details) = externalAllergy
        Allergy(id, name, severity, details)
    }

    fun transform(allergy: Allergy, customerHash: String): ExternalAllergy = allergy.let {
        val (id, name, severity, details) = allergy
        ExternalAllergy(id, name, severity, details, customerHash)
    }
}