package com.meetingdoctors.chat.presentation.entitiesextension

import android.content.Context
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.domain.DOCTOR_ROLE_DOCTOR
import com.meetingdoctors.chat.domain.DOCTOR_ROLE_MEDICAL_SUPPORT
import com.meetingdoctors.chat.domain.DOCTOR_STATUS_OFFLINE
import com.meetingdoctors.chat.domain.DOCTOR_STATUS_ONLINE
import com.meetingdoctors.chat.domain.entities.Doctor
import com.meetingdoctors.chat.domain.entities.Role
import com.meetingdoctors.chat.domain.entities.Schedule
import com.meetingdoctors.chat.presentation.entitiesextensions.formatDate
import com.meetingdoctors.chat.presentation.entitiesextensions.getNameOfDateDayWeek
import com.meetingdoctors.chat.presentation.entitiesextensions.getNextSchedule
import com.meetingdoctors.chat.presentation.entitiesextensions.getTextSchedule
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isEmptyString
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.util.*
import kotlin.collections.ArrayList

object ScheduleDoctorSpekTest : Spek({

    // Retrieve required strings from mocked context.
    val context: Context = mock {
        on { getString(R.string.meetingdoctors_locale)} doReturn "ES"
        on { getString(R.string.meetingdoctors_home_schedule_1)} doReturn meetingdoctors_HOME_SCHEDULE_1
        on { getString(R.string.meetingdoctors_home_schedule_2)} doReturn meetingdoctors_HOME_SCHEDULE_2
        on { getString(R.string.meetingdoctors_home_schedule_3)} doReturn meetingdoctors_HOME_SCHEDULE_3
        on { getString(R.string.meetingdoctors_home_schedule_4)} doReturn meetingdoctors_HOME_SCHEDULE_4
        on { getString(R.string.meetingdoctors_doctor_schedule_1)} doReturn meetingdoctors_DOCTOR_SCHEDULE_1
        on { getString(R.string.meetingdoctors_doctor_schedule_2)} doReturn meetingdoctors_DOCTOR_SCHEDULE_2
        on { getString(R.string.meetingdoctors_doctor_schedule_not_available)} doReturn meetingdoctors_DOCTOR_SCHEDULE_NOT_AVAILABLE
    }

    // Sample weekly schedule, with mixed active and inactive days, and one and two periods.
    val sampleWeeklySchedule = listOf(
            Schedule(99, 66, "monday", 540, 780, 780, 1320, 1),
            Schedule(100, 66, "tuesday", 540, 660, 750, 870, 1),
            Schedule(101, 66, "wednesday", 540, 780, 780, 1320, 1),
            Schedule(102, 66, "thursday", 540, 660, 750, 870, 1),
            Schedule(103, 66, "friday", 540, 780, 780, 1320, 1),
            Schedule(104, 66, "saturday", 540, 780, 780, 1320, 0),
            Schedule(105, 66, "sunday", 540, 780, 780, 1320, 0)
    )

    // Sample doctor with default values.
    val sampleDoctor = Doctor(0L, "", DOCTOR_STATUS_OFFLINE, "", "", "",
            "", "", 0, "", "", 0, 0,
            null, null, null,  null, null, null, ArrayList())

    lateinit var messageToReturn : String

    given("a Doctor with Offline status") {
        val doctorOfflineWithNoNextConnection = sampleDoctor.copy(app_role = Role(DOCTOR_ROLE_MEDICAL_SUPPORT, "", ""))

        on("getting next connection date but not exists") {
            messageToReturn = doctorOfflineWithNoNextConnection.getNextSchedule(context)

            it ("should return message \"No Available Schedule\"") {
                assert.that(messageToReturn, equalTo(meetingdoctors_DOCTOR_SCHEDULE_NOT_AVAILABLE))
            }
        }
    }

    given ("a Doctor with offline status") {

        val calendar : Calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, 3)

        val date : String? = calendar.formatDate(DATE_FORMAT)
        val doctorOfflineWithNoNextConnection = sampleDoctor.copy(app_role = Role(DOCTOR_ROLE_DOCTOR, "", ""),
                next_connection_at = date)

        on( "getting next connection date affirmative : ") {
            messageToReturn = doctorOfflineWithNoNextConnection.getNextSchedule(context)
        }

        it("should return message \"I will be connected at...\" like ") {
            assert.that(messageToReturn, equalTo(String.format(meetingdoctors_HOME_SCHEDULE_2,
                    calendar.formatDate(HOUR_MINUTE_FORMAT))))
        }
    }


    given("a Doctor with Online Status") {
        val onlineDoctor : Doctor

        //Create date to mock to doctor object
        val calendar : Calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, 3)

        val date : String? = calendar.formatDate(DATE_FORMAT)
        onlineDoctor = sampleDoctor.copy(status = DOCTOR_STATUS_ONLINE,
                next_disconnection_at = date)

        on("getting next disconnection time affirmative") {
            messageToReturn = onlineDoctor.getNextSchedule(context)

            it ("should return a message \"I will be available until...\"") {
                assert.that(messageToReturn, equalTo(String.format(meetingdoctors_HOME_SCHEDULE_1,
                        calendar.formatDate("HH:mm"))))
            }
        }
    }

    given("a Doctor with Online Status") {
        val onlineDoctor : Doctor = sampleDoctor.copy(status = DOCTOR_STATUS_ONLINE)

        on("getting next disconnection time, but disconnection time not exists") {
            messageToReturn = onlineDoctor.getNextSchedule(context)
        }

        it ("should return a message with no available schedule") {
            assert.that(messageToReturn, equalTo(meetingdoctors_DOCTOR_SCHEDULE_NOT_AVAILABLE))
        }
    }

    given("A doctor with Online state & next connection/disconnection may be are missing both two") {
        val onlineDoctor : Doctor = sampleDoctor.copy(status = DOCTOR_STATUS_ONLINE,
                next_connection_at = null,
                next_disconnection_at = null)

        on("Ckecking  for next connection") {
            messageToReturn = onlineDoctor.getNextSchedule(context)

            it("Should show a message with \"No available schedule\"") {
                assert.that(messageToReturn, equalTo(meetingdoctors_DOCTOR_SCHEDULE_NOT_AVAILABLE))
            }
        }

        on("Ckecking for next disconnection") {
            messageToReturn = onlineDoctor.getNextSchedule(context)

            it("Should show a message with \"No available schedule\"") {
                assert.that(messageToReturn, equalTo(meetingdoctors_DOCTOR_SCHEDULE_NOT_AVAILABLE))
            }
        }
    }

    given("a Doctor with no schedules and on holidays") {

        // Set doctor on holidays.
        val onHolidaysDoctor = sampleDoctor.copy(on_holidays = 1)

        on("getting full schedule") {
            messageToReturn = onHolidaysDoctor.getTextSchedule(context)

            it ("should have an empty schedule") {
                assert.that(messageToReturn, equalTo(meetingdoctors_DOCTOR_SCHEDULE_NOT_AVAILABLE))
            }
        }

        on("getting next schedule") {
            messageToReturn = onHolidaysDoctor.getNextSchedule(context)

            it ("should have an empty next schedule") {
                assert.that(messageToReturn, equalTo(meetingdoctors_DOCTOR_SCHEDULE_NOT_AVAILABLE))

            }
        }

    }

    given("a Doctor with schedules and on holidays") {

        // Set doctor on holidays and with a sample weekly schedule.
        val onHolidaysDoctor = sampleDoctor.copy(on_holidays = 1)

        on("getting full schedule") {
            messageToReturn = onHolidaysDoctor.getTextSchedule(context)

            it ("should have an empty schedule") {
                assert.that(messageToReturn, equalTo(meetingdoctors_DOCTOR_SCHEDULE_NOT_AVAILABLE))
            }
        }

        on("getting next schedule") {
            val schedule = onHolidaysDoctor.getNextSchedule(context)

            it ("should have an empty next schedule") {
                assert.that(schedule, equalTo(meetingdoctors_DOCTOR_SCHEDULE_NOT_AVAILABLE))
            }
        }

    }

    given("a Doctor with non active schedules") {

        // Use a schedule with all its components inactive.
        val inactiveSampleSchedule = sampleWeeklySchedule.map { it.copy(active = 0) }
        val doctor = sampleDoctor.copy(app_role = Role(DOCTOR_ROLE_DOCTOR, "", ""),
                schedules = inactiveSampleSchedule)

        on("getting full schedule") {
            val schedule = doctor.getTextSchedule(context)

            it ("should have an empty schedule") {
                assert.that(schedule, isEmptyString)
            }
        }

        on("getting next schedule") {
            messageToReturn = doctor.getNextSchedule(context)

            it ("should have an empty next schedule") {
                assert.that(messageToReturn, equalTo(meetingdoctors_DOCTOR_SCHEDULE_NOT_AVAILABLE))
            }
        }

    }

    given("a Doctor with active schedules") {

        // Set doctor with a sample weekly schedule.
        val doctor = sampleDoctor.copy(schedules = sampleWeeklySchedule,
                app_role = Role(DOCTOR_ROLE_DOCTOR, "", ""))

        on("getting full schedule") {
            messageToReturn = doctor.getTextSchedule(context, 0) // GMT + 1

            it ("should have a non empty schedule") {
                assert.that(messageToReturn, !isEmptyString)
            }

            it ("should have expected schedule") {
                assert.that(messageToReturn, equalTo(
                        "Lunes: de 9:00 a 22:00\n" +
                                "Martes: de 9:00 a 11:00 y de 12:30 a 14:30\n" +
                                "Miércoles: de 9:00 a 22:00\n" +
                                "Jueves: de 9:00 a 11:00 y de 12:30 a 14:30\n" +
                                "Viernes: de 9:00 a 22:00"))
            }

            it ("should contain active days") {
                assert.that(messageToReturn, containsSubstring("Lunes")
                        and containsSubstring("Martes")
                        and containsSubstring("Miércoles")
                        and containsSubstring("Jueves")
                        and containsSubstring("Viernes"))
            }

            it ("should not contain inactive days") {
                assert.that(messageToReturn, ! containsSubstring("Sábado")
                        and !containsSubstring("Domingo"))
            }
        }

        on("getting next schedule") {
            messageToReturn = doctor.getNextSchedule(context)

            it ("should have a non empty next schedule") {
                assert.that(messageToReturn, !isEmptyString)
            }
        }

    }

    given("an offline support Doctor") {

        val doctor = sampleDoctor.copy(status = DOCTOR_STATUS_OFFLINE,
                app_role = Role(DOCTOR_ROLE_MEDICAL_SUPPORT, "", ""),
                schedules = sampleWeeklySchedule)

        on("getting next schedule") {
            messageToReturn = doctor.getNextSchedule(context)

            it ("should have an empty next schedule") {
                assert.that(messageToReturn, equalTo(meetingdoctors_DOCTOR_SCHEDULE_NOT_AVAILABLE))
            }
        }

    }

    given("an offline Doctor") {
        val calendar : Calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, 25)

        val date : String? = calendar.formatDate(DATE_FORMAT )

        val offlineDoctor = sampleDoctor.copy(app_role = Role(DOCTOR_ROLE_DOCTOR, "", ""),
                status = DOCTOR_STATUS_OFFLINE,
                next_connection_at = date,
                schedules = sampleWeeklySchedule)

        on("getting next schedule to  next day, 1 day ahead") {
            messageToReturn = offlineDoctor.getNextSchedule(context)

            it ("should have expected next schedule") {
                assert.that(messageToReturn, equalTo(String.format(meetingdoctors_HOME_SCHEDULE_3,
                        (calendar.formatDate(HOUR_MINUTE_FORMAT)))))
            }
        }
    }

    given ("an offline Doctor") {
        val calendar : Calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, 72)

        val date : String? = calendar.formatDate(DATE_FORMAT)

        val offlineDoctor = sampleDoctor.copy(app_role = Role(DOCTOR_ROLE_DOCTOR, "", ""),
                status = DOCTOR_STATUS_OFFLINE,
                timezone = TIMEZONE_EUROPE_MADRID,
                next_connection_at = date,
                schedules = sampleWeeklySchedule)

        on("getting next schedule to 3 days ahead") {
            messageToReturn = offlineDoctor.getNextSchedule(context, "", 0, Calendar.SATURDAY)

            it ("should show a with \"i will be connected [day] at [hour]") {
                assert.that(messageToReturn, equalTo(String.format(meetingdoctors_HOME_SCHEDULE_4,
                        date.getNameOfDateDayWeek(DATE_FORMAT, offlineDoctor.timezone),
                        calendar.formatDate(HOUR_MINUTE_FORMAT))))
            }
        }
    }
})