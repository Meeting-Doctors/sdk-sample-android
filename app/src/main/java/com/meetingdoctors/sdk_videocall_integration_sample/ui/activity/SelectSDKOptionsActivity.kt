package com.meetingdoctors.sdk_videocall_integration_sample.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.meetingdoctors.chat.MeetingDoctorsClient
import com.meetingdoctors.sdk_videocall_integration_sample.BuildConfig
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
        initializeSDKAndRegisterPushToken(onSuccess = {
            MeetingDoctorsManager.initVideocallSDK(this, makeVideocallRequest = true)

        }, onError = {
            includedLoader?.visibility = View.GONE
            Toast.makeText(this, "Authenticate Error", Toast.LENGTH_LONG).show()

        })
    }

    private fun onChatButtonPressed() {
        includedLoader?.visibility = View.VISIBLE
        MeetingDoctorsManager.initializeChatSDK(this.application)
        initializeSDKAndRegisterPushToken(onSuccess = {
            val intent: Intent = MainActivity.getIntent(this)
            this.startActivity(intent)
        }, onError = {
            includedLoader?.visibility = View.GONE
            Toast.makeText(this, "Authenticate Error", Toast.LENGTH_LONG).show()
        })
    }

    private fun initializeSDKAndRegisterPushToken(onSuccess: () -> Unit, onError: () -> Unit) {
        if (MeetingDoctorsClient.instance?.isAuthenticated == true) {
            onSuccess()
        } else {
            MeetingDoctorsClient.instance?.authenticate(BuildConfig.YOUR_TOKEN,
                object : MeetingDoctorsClient.AuthenticationListener {
                    override fun onAuthenticated() {
                        onSuccess()
                    }

                    override fun onAuthenticationError(throwable: Throwable) {
                        Log.e("Authenticate ERROR", "Throwable: ${throwable.localizedMessage}")
                        onError()
                    }
                })
        }
    }

    override fun onStop() {
        super.onStop()
        includedLoader?.visibility = View.GONE

    }
}