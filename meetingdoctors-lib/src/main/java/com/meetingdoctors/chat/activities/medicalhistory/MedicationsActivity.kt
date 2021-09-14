package com.meetingdoctors.chat.activities.medicalhistory

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.TitleBarBaseActivity
import com.meetingdoctors.chat.adapters.MedicationsAdapter
import com.meetingdoctors.chat.data.Repository.Companion.instance
import com.meetingdoctors.chat.views.MedicalHistoryListTitleBar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.mediquo_activity_medications.*
import kotlinx.android.synthetic.main.mediquo_layout_medical_history_list_title_bar.*

/**
 * Created by Héctor Manrique on 4/15/21.
 */

class MedicationsActivity : TitleBarBaseActivity() {
    lateinit var medicationsAdapter: MedicationsAdapter
    private var loading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleBar(MedicalHistoryListTitleBar(this, getString(R.string.meetingdoctors_medical_history_medications)))
        setContentView(R.layout.mediquo_activity_medications)
        medicationsAdapter = MedicationsAdapter(this)
        listview?.adapter = medicationsAdapter
        listview?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            launchMedication(this@MedicationsActivity, medicationsAdapter.getItem(position)!!.id)
        }
        add_button?.setOnClickListener { launchNewMedication(this@MedicationsActivity) }
        add_button_empty?.setOnClickListener { launchNewMedication(this@MedicationsActivity) }
    }

    override fun onReady() {
        super.onReady()
        refresh()
    }

    @SuppressLint("CheckResult")
    private fun refresh() {
        loading = true
        instance?.getMedicalHistoryRepository()
                ?.getMedications()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ medications ->
                    loading = false
                    if (medications != null) {
                        if (medications.isEmpty()) {
                            findViewById<View>(R.id.empty_layout).visibility = View.VISIBLE
                            findViewById<View>(R.id.separator).visibility = View.GONE
                            listview?.visibility = View.GONE
                        } else {
                            findViewById<View>(R.id.separator).visibility = View.VISIBLE
                            listview?.visibility = View.VISIBLE
                            findViewById<View>(R.id.empty_layout).visibility = View.GONE
                            medicationsAdapter.setMedications(medications)
                        }
                    }
                }) { throwable ->
                    loading = false
                    throwable?.printStackTrace()
                }
    }
}