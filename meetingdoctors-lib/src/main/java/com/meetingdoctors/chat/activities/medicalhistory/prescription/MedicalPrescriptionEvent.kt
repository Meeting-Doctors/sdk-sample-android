package com.meetingdoctors.chat.activities.medicalhistory.prescription

sealed class MedicalPrescriptionEvent {
    object DownloadPrescriptionSuccess : MedicalPrescriptionEvent()
    object DownloadPrescriptionError : MedicalPrescriptionEvent()
}