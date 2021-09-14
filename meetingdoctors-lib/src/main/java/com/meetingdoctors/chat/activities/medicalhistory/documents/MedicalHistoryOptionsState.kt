package com.meetingdoctors.chat.activities.medicalhistory.documents

import com.meetingdoctors.chat.domain.entities.MedicalHistoryOption


sealed class MedicalHistoryOptionsState {
    data class Success(val medicalHistoryOptionsList: List<MedicalHistoryOption>) : MedicalHistoryOptionsState()
}