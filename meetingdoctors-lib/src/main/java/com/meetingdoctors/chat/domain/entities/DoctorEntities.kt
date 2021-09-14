package com.meetingdoctors.chat.domain.entities

import androidx.annotation.IntDef
import androidx.annotation.Keep
import com.meetingdoctors.chat.domain.*

@Keep
data class Doctor(var id: Long,
                           var hash: String,
                           var status: String,
                           var avatar: String,
                           var name: String,
                           var title: String,
                           var title_html: String,
                           var overview: String,
                           var on_holidays: Int,
                           var saturated: String,
                           var timezone: String,
                           var timezone_offset: Int,
                           var score: Int,
                           var next_connection_at: String?,
                           var next_disconnection_at: String?,
                           var room: Room?,
                           var app_role: Role?,
                           var speciality: Speciality?,
                           var collegiate_number: String?,
                           var schedules: List<Schedule>,
                           var is_vc_available: Boolean = false)

@kotlin.annotation.Retention
@IntDef(DOCTOR_ROLE_COMMERCIAL, DOCTOR_ROLE_ADMINISTRATIVE, DOCTOR_ROLE_DOCTOR,
        DOCTOR_ROLE_MEDICAL_SUPPORT, DOCTOR_ROLE_FREEMIUM_DOCTOR)
annotation class RoleTypes

@Keep
data class Schedule(val id: Int,
                        val user_id: Int,
                        val day: String,
                        val start_first_period: Int,
                        val end_first_period: Int,
                        val start_second_period: Int,
                        val end_second_period: Int,
                        val active: Int)

@Keep
class Speciality {
    var id: Int? = null
    var name: String? = null
}

@Keep
data class Room(var id: Int,
                    val last_message : Message,
                    val pending_messages: Long)

@Keep
data class Message(val string: String,
                             val type: String,
                             val created_at: String)

@Keep
class Role(@RoleTypes val id:Int,
                    val name : String,
                    val overview : String)