package com.meetingdoctors.chat.domain.entities

import androidx.annotation.Keep

@Keep
data class Prescription(val lastModifiedDate: String,
                        val lastModifiedHour: String,
                        val prescriptionUrl: String)