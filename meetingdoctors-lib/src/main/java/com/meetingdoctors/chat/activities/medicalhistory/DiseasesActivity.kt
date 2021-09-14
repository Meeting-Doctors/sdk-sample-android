package com.meetingdoctors.chat.activities.medicalhistory

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.TitleBarBaseActivity
import com.meetingdoctors.chat.adapters.DiseasesAdapter
import com.meetingdoctors.chat.data.Repository.Companion.instance
import com.meetingdoctors.chat.views.MedicalHistoryListTitleBar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.mediquo_activity_diseases.*
import kotlinx.android.synthetic.main.mediquo_layout_medical_history_list_title_bar.*


/**
 * Created by Héctor Manrique on 4/15/21.
 */

class DiseasesActivity : TitleBarBaseActivity() {
    private lateinit var diseasesAdapter: DiseasesAdapter
    private var loading = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleBar(MedicalHistoryListTitleBar(this, getString(R.string.meetingdoctors_medical_history_diseases)))
        setContentView(R.layout.mediquo_activity_diseases)
        diseasesAdapter = DiseasesAdapter(this)
        listview?.adapter = diseasesAdapter
        listview?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            launchDisease(this@DiseasesActivity, diseasesAdapter.getItem(position)!!.id)
        }
        add_button?.setOnClickListener { launchNewDisease(this@DiseasesActivity) }
        add_button_empty?.setOnClickListener { launchNewDisease(this@DiseasesActivity) }
    }

    override fun onReady() {
        super.onReady()
        refresh()
    }

    @SuppressLint("CheckResult")
    private fun refresh() {
        loading = true
        instance?.getMedicalHistoryRepository()
                ?.getDiseases()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ diseases ->
                    loading = false
                    if (diseases != null) {
                        if (diseases.isEmpty()) {
                            empty_layout?.visibility = View.VISIBLE
                            separator?.visibility = View.GONE
                            listview?.visibility = View.GONE
                        } else {
                            separator?.visibility = View.VISIBLE
                            listview?.visibility = View.VISIBLE
                            empty_layout?.visibility = View.GONE
                            diseasesAdapter.setDiseases(diseases)
                        }
                    }
                }) { throwable ->
                    loading = false
                    throwable?.printStackTrace()
                }
    }
}
