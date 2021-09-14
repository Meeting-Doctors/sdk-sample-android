@file:JvmName("DoctorPresentationUtils")

package com.meetingdoctors.chat.presentation.entitiesextensions

import android.content.Context
import androidx.annotation.Nullable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.domain.DATE_FORMAT
import com.meetingdoctors.chat.domain.DOCTOR_ROLE_MEDICAL_SUPPORT
import com.meetingdoctors.chat.domain.DOCTOR_STATUS_ONLINE
import com.meetingdoctors.chat.domain.SCHEDULE_ACTIVE
import com.meetingdoctors.chat.domain.entities.Doctor
import java.text.DateFormatSymbols
import java.util.*

@Nullable
internal fun Doctor.getNextSchedule(context: Context,
                                    hisCurrentDayOfWeekName: String? = getCurrentDayOfWeekName(-getTimeZoneOffsetInMinutes() + getOffsetOfSpecificTimezoneInMinutes(timezone)),
                                    hisCurrentMinute: Long = (((getCurrentMinute() - getTimeZoneOffsetInMinutes() + getOffsetOfSpecificTimezoneInMinutes(timezone)).toLong())),
                                    fromHisTimezoneToMine: Int = -(getOffsetOfSpecificTimezoneInMinutes(timezone)) + getTimeZoneOffsetInMinutes(),
                                    currentDayOfWeekIndex: Int = getCurrentDayOfWeekIndex(-getTimeZoneOffsetInMinutes() + getOffsetOfSpecificTimezoneInMinutes(timezone))) : String {

    if (on_holidays == 1) {
        return context.getString(R.string.meetingdoctors_doctor_schedule_not_available)
    }

    if (status == DOCTOR_STATUS_ONLINE) {

        val currentDay : String? = getCurrentDay(timezone)
        val scheduleDateDay : String? = next_disconnection_at.getNameOfDateDayWeek(DATE_FORMAT, timezone)

        if (currentDay.equals(scheduleDateDay, ignoreCase = true)) {
            return String.format(context.getString(R.string.meetingdoctors_home_schedule_1),
                    getTimeFromDate(next_disconnection_at, DATE_FORMAT, timezone))
        }

        return context.getString(R.string.meetingdoctors_doctor_schedule_not_available)
    } else if (app_role!!.id != DOCTOR_ROLE_MEDICAL_SUPPORT) {

        val currentDay : String? = getCurrentDay(timezone)
        val scheduleDateDay : String? = next_connection_at?.getNameOfDateDayWeek(DATE_FORMAT, timezone)

        if (currentDay.equals(scheduleDateDay, ignoreCase = true)) {
            return String.format(context.getString(R.string.meetingdoctors_home_schedule_2),
                    getTimeFromDate(next_connection_at, DATE_FORMAT, timezone))
        }

        var tomorrow = true

        for (i in currentDayOfWeekIndex..13) {
            val dayOfWeekIndex = i % 7
            val weekDays = DateFormatSymbols(Locale.US).weekdays

            for (schedule in schedules) {

                if (schedule.active == SCHEDULE_ACTIVE && scheduleDateDay.equals(weekDays[dayOfWeekIndex + 1],
                                ignoreCase = true)) {
                    return if (tomorrow) {
                        String.format(context.getString(R.string.meetingdoctors_home_schedule_3),
                                getTimeFromDate(next_connection_at, DATE_FORMAT, timezone))
                    } else {
                        return String.format(context.getString(R.string.meetingdoctors_home_schedule_4),
                                getDayOfWeekName(dayOfWeekIndex + 1, Locale(context.getString(R.string.meetingdoctors_locale))),
                                getTimeFromDate(next_connection_at, DATE_FORMAT, timezone))
                    }
                }
            }
            tomorrow = false
        }
    }

    return context.getString(R.string.meetingdoctors_doctor_schedule_not_available)
}

internal fun Doctor.getTextSchedule(context: Context, myTimeZoneOffsetInMinutes: Int = getTimeZoneOffsetInMinutes()) : String {
    if (schedules.isEmpty() || on_holidays == 1) {
        return context.getString(R.string.meetingdoctors_doctor_schedule_not_available)
    }
    val adjustedOffSet = -timezone_offset /* to gmt+0 */ + myTimeZoneOffsetInMinutes /* to my gmt */
    val fromHisTimezoneToMine = when (timezone == "Europe/Madrid") {
        true -> 0
        false -> adjustedOffSet
    }
 //   val fromHisTimezoneToMine = -(getOffsetOfSpecificTimezoneInMinutes(timezone)) + getTimeZoneOffsetInMinutes()
    var textSchedule = ""

    // try grouping monday to friday timetable
    if ((schedules.filter { it -> it.active == 1 }).size == 5 && schedules[0].day.equals("monday")) {
        for (i in 1..4) {
            val schedule = schedules[i]
            if(getDayOfWeekFromName(schedule.day) == getDayOfWeekFromName(schedules[i - 1].day)) break
            if(schedule.start_first_period != schedules[i - 1].start_first_period) break
            if(schedule.end_first_period != schedules[i - 1].end_first_period) break
            if(schedule.start_second_period != schedules[i - 1].start_second_period) break
            if(schedule.end_second_period != schedules[i - 1].end_second_period) break
            if(i == 4) {
                var localizeDayOfWeek: String? = getLocalizeDayOfWeek(context, schedule.day)
                localizeDayOfWeek ?: break
                localizeDayOfWeek = localizeDayOfWeek.substring(0, 1).toUpperCase() + localizeDayOfWeek.substring(1)
                if (schedule.end_first_period < schedule.start_second_period) {
                    return String.format(context.getString(R.string.meetingdoctors_doctor_schedule_4),
                            localizeDayOfWeek,
                            formatTimeFromMinutes(schedule.start_first_period  + fromHisTimezoneToMine),
                            formatTimeFromMinutes(schedule.end_first_period  + fromHisTimezoneToMine),
                            formatTimeFromMinutes(schedule.start_second_period  + fromHisTimezoneToMine),
                            formatTimeFromMinutes(schedule.end_second_period  + fromHisTimezoneToMine))
                } else {
                    return String.format(context.getString(R.string.meetingdoctors_doctor_schedule_3),
                            localizeDayOfWeek,
                            formatTimeFromMinutes(schedule.start_first_period  + fromHisTimezoneToMine),
                            formatTimeFromMinutes(schedule.end_second_period  + fromHisTimezoneToMine))
                }
            }
        }

    }

    // generate day by day timetable
    for (schedule in schedules) {
        if (schedule.active == 1) {
            var localizeDayOfWeek: String? = getLocalizeDayOfWeek(context, schedule.day)
            if (localizeDayOfWeek != null) {
                localizeDayOfWeek = localizeDayOfWeek.substring(0, 1).toUpperCase() + localizeDayOfWeek.substring(1)

                textSchedule = if (textSchedule.isEmpty()) {
                    ""
                } else {
                    textSchedule + "\n"
                }

                if (schedule.end_first_period < schedule.start_second_period) {
                    textSchedule += String.format(context.getString(R.string.meetingdoctors_doctor_schedule_2),
                            localizeDayOfWeek,
                            formatTimeFromMinutes(schedule.start_first_period  + fromHisTimezoneToMine),
                            formatTimeFromMinutes(schedule.end_first_period  + fromHisTimezoneToMine),
                            formatTimeFromMinutes(schedule.start_second_period  + fromHisTimezoneToMine),
                            formatTimeFromMinutes(schedule.end_second_period  + fromHisTimezoneToMine))
                } else {
                    textSchedule += String.format(context.getString(R.string.meetingdoctors_doctor_schedule_1),
                            localizeDayOfWeek,
                            formatTimeFromMinutes(schedule.start_first_period  + fromHisTimezoneToMine),
                            formatTimeFromMinutes(schedule.end_second_period  + fromHisTimezoneToMine))
                }
            }
        }
    }

    return textSchedule
}

internal fun arrayFromJson(json : String) : List<Doctor> {
    return try {
        val listType = object : TypeToken<ArrayList<Doctor>>() {}.type

        return Gson().fromJson(json, listType)
    } catch (e: Exception) {
        e.printStackTrace()
        ArrayList()
    }
}