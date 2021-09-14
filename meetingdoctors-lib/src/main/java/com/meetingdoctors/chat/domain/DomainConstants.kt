@file:JvmName("DomainConstants")

package com.meetingdoctors.chat.domain

const val DOCTOR_STATUS_OFFLINE = "offline"
const val DOCTOR_STATUS_ONLINE = "online"

enum class DoctorSaturatedStatus(val label: String) {
    NONE("none"),
    HIGH("high")
}

const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ"

internal const val DOCTOR_ROLE_COMMERCIAL = 1
internal const val DOCTOR_ROLE_ADMINISTRATIVE = 2
internal const val DOCTOR_ROLE_DOCTOR = 3
internal const val DOCTOR_ROLE_MEDICAL_SUPPORT = 4
internal const val DOCTOR_ROLE_FREEMIUM_DOCTOR = 5

internal const val SCHEDULE_ACTIVE = 1
