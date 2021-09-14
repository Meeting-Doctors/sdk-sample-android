package com.meetingdoctors.chat


/**
 * Created by Héctor Manrique on 4/7/21.
 */

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meetingdoctors.chat.data.Repository
import java.net.URLEncoder

class InstallReferrerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val referrer = intent.getStringExtra("referrer")
        referrer?.let {
            try {
                Repository.instance?.setReferrer(URLEncoder.encode(referrer, "utf-8"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}