package com.meetingdoctors.chat.helpers

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import com.meetingdoctors.chat.data.Speciality
import com.meetingdoctors.chat.domain.entities.Doctor
import java.util.*


/**
 * Created by Héctor Manrique on 4/8/21.
 */

class SystemHelper {
    companion object {
        fun dpToPixel(context: Context, dp: Int): Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
        }

        fun hideKeyboard(activity: Activity) {
            val view = activity.currentFocus
            if (view != null) {
                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        fun isConnected(context: Context): Boolean {
            val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

        fun getVersionCode(context: Context): Int? {
            try {
                return context.packageManager.getPackageInfo(context.packageName, 0).versionCode
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return null
        }


        fun getApplicationName(context: Context): String {
            val applicationInfo = context.applicationInfo
            val stringId = applicationInfo.labelRes
            return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(stringId)
        }

        fun getCountryIso(context: Context): String? {
            val telephonyManager = context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val country = if (telephonyManager != null) telephonyManager.networkCountryIso else context.resources.configuration.locale.country
            return if (country != null && country.isNotEmpty()) country.toLowerCase() else "es"
        }

        fun getLanguageIso(): String {
            val language = Locale.getDefault().language
            return language.toLowerCase()
        }

        fun isPermissionDeclared(context: Context, permission: String): Boolean {
            try {
                val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
                if (info.requestedPermissions != null) {
                    for (p in info.requestedPermissions) {
                        if (p == permission) return true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }
    }
}
