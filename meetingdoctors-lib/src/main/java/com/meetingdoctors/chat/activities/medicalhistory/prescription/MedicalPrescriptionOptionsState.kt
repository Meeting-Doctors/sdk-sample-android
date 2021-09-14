package com.meetingdoctors.chat.activities.medicalhistory.prescription

import com.meetingdoctors.chat.domain.entities.Prescription


sealed class MedicalPrescriptionOptionsState {
    data class Success(val prescription: Prescription) : MedicalPrescriptionOptionsState()
    data class Error(val throwable: Throwable): MedicalPrescriptionOptionsState()
}