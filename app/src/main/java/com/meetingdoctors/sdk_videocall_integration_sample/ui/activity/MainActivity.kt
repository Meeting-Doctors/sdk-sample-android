package com.meetingdoctors.sdk_videocall_integration_sample.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.meetingdoctors.chat.MeetingDoctorsClient
import com.meetingdoctors.chat.views.ProfessionalList.*
import com.meetingdoctors.sdk_videocall_integration_sample.BuildConfig
import com.meetingdoctors.sdk_videocall_integration_sample.R
import kotlinx.android.synthetic.main.layout_main_activity.*


class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_main_activity)

        authenticateCustomerSDK(BuildConfig.YOUR_TOKEN)
        setProfessionalListener()

//        val crashButton = Button(this)
//        crashButton.text = "Test Crash"
//        crashButton.setOnClickListener {
//            throw RuntimeException("Test Crash") // Force a crash
//        }
//
//        addContentView(crashButton, ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT))
    }


    private fun authenticateCustomerSDK(userToken: String?) {
        MeetingDoctorsClient.instance?.authenticate(userToken = userToken,
                authenticationListener = object: MeetingDoctorsClient.AuthenticationListener {
                    override fun onAuthenticated() {
                        Log.i("Authenticate", "Authenticate succesful")
                    }

                    override fun onAuthenticationError(throwable: Throwable) {
                        Log.e("Authenticate()", "Exception: ${throwable.localizedMessage}")
                    }
                })
    }

    private fun setProfessionalListener() {
        customer_sdk_professional_list.setProfessionalListListener(object :ProfessionalListListener {
            override fun onProfessionalClick(
                professionalId: Long,
                speciality: String?,
                hasAccess: Boolean,
                isSaturated: Boolean
            ): Boolean {
                /* professionalId: professional id
               speciality: professional speciality.
               hasAccess: whether the user can chat with this professional.
                  if hasAccess is false return true would be ignored.
               isSaturated: possible delay on answers
               return value:
                  true: proceed with chat openning.
                  false: cancel chat openning. */
                return true
            }

            override fun onListLoaded() {
                /* called after professional list if loaded */
            }

            override fun onUnreadMessageCountChange(unreadMessageCount: Long) {
                /* called when unread message count change */
            }
        })
    }

    private fun authenticateVideoCallSDK(userToken: String?) {

    }
}