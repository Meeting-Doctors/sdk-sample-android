package com.meetingdoctors.chat.activities.medicalhistory

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.meetingdoctors.chat.MeetingDoctorsClient.Companion.instance
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.MenuBaseActivity
import com.meetingdoctors.chat.activities.medicalhistory.documents.MyDocumentsActivity.Companion.newInstance
import com.meetingdoctors.chat.activities.medicalhistory.utils.MedicalHistoryVisibilityOptions
import com.meetingdoctors.chat.adapters.MedicalHistoryAdapter
import com.meetingdoctors.chat.data.Cache.Companion.getSetup
import com.meetingdoctors.chat.domain.entities.Options
import com.meetingdoctors.chat.views.HomeTitleBar
import kotlinx.android.synthetic.main.mediquo_activity_medical_history.*
import java.util.*

/**
 * Created by Héctor Manrique on 4/15/21.
 */

class MedicalHistoryActivity : MenuBaseActivity() {
    private val visibilityOptionsMap = HashMap<MedicalHistoryVisibilityOptions, Boolean>()
    private var homeTitleBar: HomeTitleBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeTitleBar = HomeTitleBar(this, getString(R.string.meetingdoctors_medical_history_title))
        setTitleBar(homeTitleBar)
        setContentView(R.layout.mediquo_activity_medical_history)
        setVisibilityOptions()
        val medicalHistoryAdapter = MedicalHistoryAdapter(this@MedicalHistoryActivity)
        listview?.adapter = medicalHistoryAdapter
        val finalVisibilityOptionsMap: Map<MedicalHistoryVisibilityOptions, Boolean> = visibilityOptionsMap
        listview?.onItemClickListener = AdapterView.OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            when (position) {
                0 -> launchAllergies(this)
                1 -> launchDiseases(this)
                2 -> launchMedications(this)
                3 -> newInstance(this, finalVisibilityOptionsMap)
            }
        }
        setCustomTitleBarView()
    }

    private fun setCustomTitleBarView() {
        if (homeTitleBar != null &&
                instance?.getMenuBaseCustomViewWrapper() != null) {
            instance?.getMenuBaseCustomViewWrapper()
                    ?.setView(this@MedicalHistoryActivity, homeTitleBar!!)
        }
    }

    private fun setVisibilityOptions() {
        val setUp = getSetup(this)
        var medicalHistoryOptions: Options? = null
        if (setUp?.medicalHistory?.options != null) {
            medicalHistoryOptions = setUp.medicalHistory.options
        }
        var isVisible = true
        for (option in MedicalHistoryVisibilityOptions.values()) {
            when (option) {
                MedicalHistoryVisibilityOptions.reports -> isVisible = medicalHistoryOptions?.hasMedicalReports
                        ?: false
                MedicalHistoryVisibilityOptions.referrals -> isVisible = medicalHistoryOptions?.hasMedicalDerivations
                        ?: false
                MedicalHistoryVisibilityOptions.prescriptions -> isVisible = medicalHistoryOptions?.hasPrescription
                        ?: false
                else -> {
                }
            }
            visibilityOptionsMap[option] = isVisible
        }
    }

}
