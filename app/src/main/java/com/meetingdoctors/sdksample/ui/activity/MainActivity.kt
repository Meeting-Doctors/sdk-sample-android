package com.meetingdoctors.sdksample.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.meetingdoctors.chat.MeetingDoctorsClient
import com.meetingdoctors.chat.views.professionallilst.ProfessionalList.*
import com.meetingdoctors.chat.views.professionallilst.ProfessionalListener
import com.meetingdoctors.mdsecure.sharedpref.OnResetDataListener
import com.meetingdoctors.sdksample.R
import com.meetingdoctors.sdksample.utils.MeetingDoctorsManager
import kotlinx.android.synthetic.main.layout_main_activity.*


class MainActivity : AppCompatActivity() {

    companion object {

        @JvmStatic
        fun getIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_main_activity)

        /**
         * Integrator can listen for some useful callbacks
         */
        setProfessionalListener()
        /**
         * Access extra options, like logout feature or Medical History screens
         */
        setUpBottomNavigationBar()

        MeetingDoctorsManager.initVideocallSDK(this, makeVideocallRequest = false)
        /**
         * This action its ONLY required for 1-to-1 Video call requests.
         * At meetingDoctorsManager you can find the implementation of OnVideoCallRequest
         * interface, wich handles behavior for this concrete video call request type
         */
        MeetingDoctorsManager.setVideoCallRequestlistener()

        refresh_list_button.setOnClickListener {
            customer_sdk_professional_list?.refreshList()
        }
    }

    private fun setUpBottomNavigationBar() {
        bottom_navigation?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.medicalHistory -> {
                    MeetingDoctorsClient.instance?.openMedicalHistory(this@MainActivity)
                    true
                }
                R.id.logout -> {
                    MeetingDoctorsClient.instance?.deauthenticate(object : OnResetDataListener {
                        override fun dataResetError(exception: Exception?) {
                            super.dataResetError(exception)
                            showError("Error at logout")
                        }

                        override fun dataResetSuccessFul() {
                            super.dataResetSuccessFul()
                            finish()
                        }
                    })
                    true
                }
                else -> false
            }
        }
    }


    private fun setProfessionalListener() {
        customer_sdk_professional_list?.setProfessionalListListener(object :
            ProfessionalListener {
            override fun onProfessionalClick(
                professionalId: Long,
                speciality: String,
                hasAccess: Boolean,
                isSaturated: Boolean
            ): Boolean {
                /**
                 * professionalId: professional id,
                 * speciality: professional speciality,
                 * hasAccess: whether the user can chat with this professional. If hasAccess is false return true would be ignored.
                 * isSaturated: possible delay on answers
                 * return value:
                 * true: proceed with chat openning.
                 * false: cancel chat openning.
                 */
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

    private fun showError(message: String) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG).show()
    }
}