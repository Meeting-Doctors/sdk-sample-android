package com.meetingdoctors.chat.activities.medicalhistory.utils

import androidx.annotation.Keep
import com.meetingdoctors.chat.activities.medicalhistory.utils.MedicalHistoryVisibilityOptions.*

@Keep
enum class MedicalHistoryVisibilityOptions {
    allergies, diseases, medication, reports, referrals, prescriptions
}

val medicalhistoryVisibilityOptionsArray = listOf(allergies, diseases, medication, reports, referrals, prescriptions)
const val visibilityOptionsIntentExtra = "visibilityOptionsIntentExtra"
