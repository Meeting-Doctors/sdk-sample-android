@file:JvmName("DoctorUtils")

package com.meetingdoctors.chat.domain.entitesextensions

import android.content.Context
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.domain.DATE_FORMAT
import com.meetingdoctors.chat.domain.DoctorSaturatedStatus
import com.meetingdoctors.chat.domain.entities.Doctor
import com.meetingdoctors.chat.domain.entities.Message
import com.meetingdoctors.chat.domain.entities.Room
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

internal fun Doctor.getLastMessage(): Message? {
    return room?.last_message
}

internal fun Doctor.getLastMessageTime(): Calendar? {
    val lastMessage: Message? = getLastMessage()

    if (room != null && lastMessage != null && lastMessage.created_at != null) {
        try {
            val simpleDateFormat = SimpleDateFormat(DATE_FORMAT)
            val calendar = Calendar.getInstance()
            calendar.time = simpleDateFormat.parse(room!!.last_message.created_at)
            return calendar
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
    return null
}

internal fun Doctor.getTitle(context: Context): String? {
    if (collegiate_number != null && Repository.instance?.getCollegiateNumbersVisibility() == true) {
        return "$title (${context.getString(R.string.meetingdoctors_collegiate_number)} $collegiate_number)"
    } else {
        return title
    }
}

internal fun Doctor.getTitleHtml(context: Context): String? {
    if (collegiate_number != null && Repository.instance?.getCollegiateNumbersVisibility() == true) {
        return "$title_html (${context.getString(R.string.meetingdoctors_collegiate_number)}&nbsp;$collegiate_number)"
    } else {
        return title_html
    }
}

internal fun Doctor.getPendingMessageCount(): Int = room?.pending_messages?.toInt() ?: 0

internal fun Doctor.getRoomId(): Int? = room?.id

internal fun Doctor.setRoomId(roomId: Int) {
    if (room != null) {
        room!!.id = roomId
    } else {
        room = Room(roomId, Message("", "", ""), 0)
    }
}

internal fun Doctor.isSaturated() = saturated == DoctorSaturatedStatus.HIGH.label


