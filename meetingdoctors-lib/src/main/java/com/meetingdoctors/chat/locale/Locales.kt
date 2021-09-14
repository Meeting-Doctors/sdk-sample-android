@file:JvmName("LocalesList")
package com.meetingdoctors.chat.locale

import java.util.*

object Locales {
    val Turkish: Locale by lazy { Locale("tr", "TR") }
    val Romanian: Locale by lazy { Locale("ro", "RO") }
    val Polish: Locale by lazy { Locale("pl", "PL") }
    val Hindi: Locale by lazy { Locale("hi", "IN") }
    val Urdu: Locale by lazy { Locale("ur", "IN") }
    val Spanish: Locale by lazy { Locale("es", "ES") }
    val Catalan: Locale by lazy { Locale("ca", "CA")}

    val RTL: Set<String> by lazy {
        hashSetOf(
            "ar",
            "dv",
            "fa",
            "ha",
            "he",
            "iw",
            "ji",
            "ps",
            "sd",
            "ug",
            "ur",
            "yi"
        )
    }
}