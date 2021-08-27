package com.meetingdoctors.sdk_videocall_integration_sample.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.meetingdoctors.sdk_videocall_integration_sample.R
import com.meetingdoctors.sdk_videocall_integration_sample.utils.MeetingDoctorsManager
import kotlinx.android.synthetic.main.activity_select_sdkoptions.*

class SelectSDKOptionsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_sdkoptions)

        videocallButton?.setOnClickListener {
            onVideocallButtonPressed()
        }
        chatButton?.setOnClickListener {
            onChatButtonPressed()
        }
    }

    private fun onVideocallButtonPressed() {
        includedLoader?.visibility = View.VISIBLE
        MeetingDoctorsManager.initializeChatSDK(application)
        MeetingDoctorsManager.initVideocallSDK(this)
    }

    private fun onChatButtonPressed() {
        includedLoader?.visibility = View.VISIBLE
        MeetingDoctorsManager.initializeChatSDK(application)
        val intent: Intent = MainActivity.getIntent(this)
        this.startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        includedLoader?.visibility = View.GONE

    }
}