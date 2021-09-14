package com.meetingdoctors.chat.data.webservices.mappers

import com.meetingdoctors.chat.data.webservices.entities.ExternalMedication
import com.meetingdoctors.chat.domain.entities.Medication

internal class MedicationMapper {

    fun transform(externalMedication: ExternalMedication): Medication = externalMedication.let {
        val (id, name, posology, details) = externalMedication
        Medication(id, name, posology ?: "", details ?: "")
    }

    fun transform(medication: Medication, patientHash: String): ExternalMedication = medication.let {
        val (id, name, posology, details) = medication
        ExternalMedication(id, name, posology, details, patientHash)
    }
}