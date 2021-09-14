package com.meetingdoctors.chat.domain.entities

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.meetingdoctors.chat.R

enum class MedicalHistoryOption(@StringRes val optionName: Int,
                                @DrawableRes val imageResource: Int) {
    VIDEOCALL_REPORT(R.string.meetingdoctors_medical_history_reports,
            R.drawable.mediquo_ic_medical_history_reports),
    DERIVATION(R.string.meetingdoctors_medical_history_referrals,
            R.drawable.mediquo_ic_medical_history_referral),
    PRESCRIPTION(R.string.meetingdoctors_medical_history_prescriptions,
            R.drawable.mediquo_ic_medical_history_prescription)
}
