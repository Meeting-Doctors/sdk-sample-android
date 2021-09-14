package com.meetingdoctors.chat.presentation.entitiesextensions

import android.content.Context
import com.meetingdoctors.chat.R
import java.text.DateFormat
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

fun String.formatToPrescriptionDate(): String {
    val originalFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ")
    val targetDateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
    val date: Date = originalFormat.parse(this)
    return targetDateFormat.format(date)
}

fun String.formatToPrescriptionHour(): String {
    val originalFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ")
    val targetHourFormat: DateFormat = SimpleDateFormat("hh:mm")
    val date: Date = originalFormat.parse(this)
    return targetHourFormat.format(date)
}

fun Date.formatDate(): String {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm")
    return dateFormat.format(this)
}

fun Date?.isSameDay(date2: Date?): Boolean {
    require(!(this == null || date2 == null)) { "The dates must not be null" }
    val cal1 = Calendar.getInstance()
    cal1.time = this
    val cal2 = Calendar.getInstance()
    cal2.time = date2
    return cal1.isSameDay(cal2)
}

fun Long.isSameDay(date2: Long): Boolean {
    val cal1 = Calendar.getInstance()
    cal1.timeInMillis = this
    val cal2 = Calendar.getInstance()
    cal2.timeInMillis = date2
    return cal1.isSameDay(cal2)
}

fun Calendar?.isSameDay(cal2: Calendar?): Boolean {
    require(!(this == null || cal2 == null)) { "The dates must not be null" }
    return this[Calendar.ERA] == cal2[Calendar.ERA]
            && this[Calendar.YEAR] == cal2[Calendar.YEAR]
            && this[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
}

fun Date?.isToday(): Boolean {
    return this.isSameDay(Calendar.getInstance().time)
}

fun calculateAge(birthDate: String?, format: String?): Int? {
    var age: Int? = null
    try {
        val formatter: DateFormat = SimpleDateFormat(format, Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = formatter.parse(birthDate)
        val today = Calendar.getInstance()
        age = today[Calendar.YEAR] - calendar[Calendar.YEAR]
        if (today[Calendar.DAY_OF_YEAR] < calendar[Calendar.DAY_OF_YEAR]) {
            age--
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return age
}

fun Date?.formatDate(format: String?): String? {
    if (this != null && format != null) {
        try {
            val formatter: DateFormat = SimpleDateFormat(format, Locale.getDefault())
            return formatter.format(this)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    return null
}

fun getTimeFromDate(date: String?, format: String?, specificTimezone: String?): String? {
    var time: String? = null
    if (date != null && format != null) {
        try {
            val formatter: DateFormat = SimpleDateFormat(format, Locale.getDefault())
            val timezone = TimeZone.getTimeZone(specificTimezone)
            val calendar = Calendar.getInstance()
            calendar.timeZone = timezone
            calendar.time = formatter.parse(date)
            time = calendar.time.formatDate("HH:mm")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    return time
}

fun Calendar?.formatDate(format: String?): String? {
    if (this != null && format != null) {
        try {
            val formatter: DateFormat = SimpleDateFormat(format, Locale.getDefault())
            return formatter.format(this.time)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    return null
}

fun String?.formatDate(formatFrom: String?, formatTo: String?): String? {
    if (formatFrom != null && formatTo != null) {
        val calendar = this.parseDate(formatFrom)
        return calendar.formatDate(formatTo)
    }
    return null
}

fun String?.parseDate(format: String?): Calendar? {
    var calendar: Calendar? = null
    if (this != null) {
        try {
            val formatter: DateFormat = SimpleDateFormat(format, Locale.getDefault())
            calendar = Calendar.getInstance()
            calendar.time = formatter.parse(this)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    return calendar
}

fun String?.getNameOfDateDayWeek(format: String?, specificTimezone: String?): String? {
    var dayName: String? = ""
    if (this != null && format != null) {
        try {
            val formatter: DateFormat = SimpleDateFormat(format, Locale.getDefault())
            val timezone = TimeZone.getTimeZone(specificTimezone)
            val calendar = Calendar.getInstance()
            calendar.timeZone = timezone
            calendar.time = formatter.parse(this)
            val currentDate = calendar.time
            dayName = SimpleDateFormat("EEEE", Locale.US)
                    .format(currentDate.time)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    return dayName
}

fun getCurrentDay(specificTimezone: String?): String? {
    var dayName: String? = null
    try {
        val calendar = Calendar.getInstance()
        val timezone = TimeZone.getTimeZone(specificTimezone)
        calendar.timeZone = timezone
        val date = calendar.time
        dayName = SimpleDateFormat("EEEE", Locale.US).format(date.time)
    } catch (e: java.lang.Exception) {
        e.localizedMessage
    }
    return dayName
}

fun getCurrentDayOfWeekName(offset: Int): String? {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MINUTE, offset) // to new gmt
    return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US)
}

fun getCurrentDayOfWeekIndex(offset: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MINUTE, offset) // to new gmt
    return calendar[Calendar.DAY_OF_WEEK]
}

fun formatTimeFromMinutes(minutes: Int): String? {
    val calendar = Calendar.getInstance()
    calendar[Calendar.HOUR_OF_DAY] = minutes / 60
    calendar[Calendar.MINUTE] = minutes % 60
    return calendar.time.formatDate("H:mm")
}

fun getTimeZoneOffsetInMinutes(): Int {
    return TimeZone.getTimeZone(TimeZone.getDefault().id)
            .getOffset(Calendar.getInstance().timeInMillis) / 60000
}

fun getOffsetOfSpecificTimezoneInMinutes(timezone: String?): Int {
    return TimeZone.getTimeZone(timezone)
            .getOffset(Calendar.ZONE_OFFSET.toLong()) / 60000
}

fun getCurrentMinute(): Int {
    val calendar = Calendar.getInstance()
    return calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]
}

fun getDayOfWeekName(index: Int, locale: Locale?): String? {
    val weekDays = DateFormatSymbols(locale).weekdays
    return weekDays[index]
}

fun getDayOfWeekFromName(name: String?): Int {
    val weekDays = DateFormatSymbols(Locale.US).weekdays
    for (i in weekDays.indices) {
        if (weekDays[i].equals(name, ignoreCase = true)) {
            return i
        }
    }
    return -1
}

fun getLocalizeDayOfWeek(context: Context, dayOfWeek: String?): String? {
    return getDayOfWeekName(getDayOfWeekFromName(dayOfWeek),
            Locale(context.getString(R.string.meetingdoctors_locale)))
}
