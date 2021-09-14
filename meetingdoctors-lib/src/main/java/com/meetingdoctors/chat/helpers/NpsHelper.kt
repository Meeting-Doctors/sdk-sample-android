package com.meetingdoctors.chat.helpers

import com.meetingdoctors.mdsecure.sharedpref.preference.SharedPreferencesHelper

internal object NpsHelper {

    fun storeNpsStatusCompleted(status: Boolean) {
        SharedPreferencesHelper.put(NPS_STATUS_PREFERENCE_KEY, status)
    }

    fun isNpsRequestCompletedByUser(): Boolean {
        return SharedPreferencesHelper.getBoolean(NPS_STATUS_PREFERENCE_KEY,
                NPS_REQUEST_COMPLETED_DEFAULT_VALUE)
    }
}

internal const val NPS_STATUS_PREFERENCE_KEY = "npsStatus"
internal const val NPS_REQUEST_COMPLETED_DEFAULT_VALUE =  true