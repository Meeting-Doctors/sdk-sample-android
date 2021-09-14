package com.meetingdoctors.chat.activities

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.bumptech.glide.Glide
import com.meetingdoctors.chat.MeetingDoctorsClient
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.TitleBarBaseActivity
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.data.Speciality.Companion.icon
import com.meetingdoctors.chat.data.Speciality.Companion.name
import com.meetingdoctors.chat.domain.entitesextensions.getTitle
import com.meetingdoctors.chat.domain.entities.Doctor
import com.meetingdoctors.chat.presentation.entitiesextensions.getTextSchedule
import com.meetingdoctors.chat.presentation.entitiesextensions.getTimeZoneOffsetInMinutes
import com.meetingdoctors.chat.views.BackTitleBar
import kotlinx.android.synthetic.main.mediquo_activity_doctor_profile.*

/**
 * Created by Héctor Manrique on 4/15/21.
 */

class DoctorProfileActivity : TitleBarBaseActivity() {
    private var doctor: Doctor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (MeetingDoctorsClient.instance == null) {
            finish()
            return
        }
        val userHash = intent.getStringExtra("doctorUserHash")
        if (userHash == null) {
            finish()
            return
        }
        doctor = Repository.instance?.getDoctorByUserHash(userHash)
        if (doctor == null) {
            finish()
            return
        }
        setTitleBar(BackTitleBar(this, doctor?.name))
        setContentView(R.layout.mediquo_activity_doctor_profile)

        val transparentMediquoBaseColor = ColorUtils.setAlphaComponent(
                ContextCompat.getColor(this,
                        R.color.meetingdoctors_base_color),
                (0.2 * 255).toInt()
        )
        (doctor_layout?.background as? ColorDrawable)?.color = transparentMediquoBaseColor
        (schedule_layout?.background as? GradientDrawable)?.setColor(transparentMediquoBaseColor)
        val transparentDescriptionColor = ColorUtils.setAlphaComponent(
                ContextCompat.getColor(this,
                        R.color.meetingdoctors_doctor_information_header_description_color),
                (0.2 * 255).toInt())
        (description_layout?.background as? GradientDrawable)?.setColor(transparentDescriptionColor)
        val displayMetrics = resources.displayMetrics
        photo?.layoutParams?.height = (displayMetrics.widthPixels.toDouble() * 0.45).toInt()
        val avatarLength = doctor?.avatar?.length ?: 0
        if (avatarLength > 0) {
            Glide.with(this).load(doctor?.avatar).into(photo)
        }
        name?.text = doctor?.name
        speciality?.text = doctor?.getTitle(this)
        description?.text = doctor?.overview
        val textSchedule = doctor?.getTextSchedule(this@DoctorProfileActivity, getTimeZoneOffsetInMinutes())
        if (textSchedule?.isNotEmpty() == true) {
            schedule?.text = textSchedule
            schedule_card?.visibility = View.VISIBLE
        } else {
            schedule_card?.visibility = View.GONE
        }
        doctor?.speciality?.id?.let {
            doctor_speciality_icon?.setImageResource(icon(it))
        }
    }

    override fun onResume() {
        super.onResume()
        doctor?.speciality?.id?.let {
            if (doctor?.hash != null) {
                Repository.instance?.getTracking()?.viewProfessionalProfile(
                        doctor?.hash!!,
                        name(it)
                )
            }

        }
    }
}
