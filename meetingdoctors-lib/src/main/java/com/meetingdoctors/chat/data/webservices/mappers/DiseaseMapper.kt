package com.meetingdoctors.chat.data.webservices.mappers

import com.meetingdoctors.chat.data.webservices.entities.ExternalDisease
import com.meetingdoctors.chat.domain.entities.Disease

internal class DiseaseMapper {

    fun transform(externalDisease: ExternalDisease): Disease = externalDisease.let {
        val (id, name, details, diagnosisDate, resolutionDate) = externalDisease
        Disease(id, name, details ?: "", diagnosisDate, resolutionDate)
    }

    fun transform(disease: Disease, patientHash: String): ExternalDisease = disease.let {
        val (id, name, details, diagnosisDate, resolutionDate) = disease
        ExternalDisease(id, name, details, diagnosisDate, resolutionDate, patientHash)
    }
}