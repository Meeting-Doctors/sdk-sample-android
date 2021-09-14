package com.meetingdoctors.chat.activities.medicalhistory.documents.adapter

import com.meetingdoctors.chat.domain.entities.MedicalHistoryOption

interface MedicalHistoryOptionClickListener {

    fun onClickOption(medicalHistoryOption: MedicalHistoryOption)
}