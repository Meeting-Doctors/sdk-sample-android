package com.meetingdoctors.chat.activities.base

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatActivity
import com.meetingdoctors.chat.locale.LocaleHelper
import java.util.*


/**
 * Created by Héctor Manrique on 3/23/21.
 */
open class LocaleManagerActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        val config = Configuration()
        applyOverrideConfiguration(config)
    }

    override fun applyOverrideConfiguration(newConfig: Configuration) {
        super.applyOverrideConfiguration(updateConfigurationIfSupported(newConfig))
    }

    open fun updateConfigurationIfSupported(config: Configuration): Configuration? {
        // Configuration.getLocales is added after 24 and Configuration.locale is deprecated in 24
        if (Build.VERSION.SDK_INT >= 24) {
            if (!config.locales.isEmpty) {
                return config
            }
        } else {
            if (config.locale != null) {
                return config
            }
        }
        val languageCode = LocaleHelper.getLanguage(this)
        val locale = Locale(languageCode)
        if (locale != null) {
            // Configuration.setLocale is added after 17 and Configuration.locale is deprecated
            // after 24
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocales(LocaleList.forLanguageTags(languageCode))
            } else {
                config.setLocale(locale)
            }
        }
        return config
    }

}