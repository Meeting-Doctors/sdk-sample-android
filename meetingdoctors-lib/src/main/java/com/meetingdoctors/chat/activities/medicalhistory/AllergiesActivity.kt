package com.meetingdoctors.chat.activities.medicalhistory

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.TitleBarBaseActivity
import com.meetingdoctors.chat.adapters.AllergiesAdapter
import com.meetingdoctors.chat.data.Repository.Companion.instance
import com.meetingdoctors.chat.views.MedicalHistoryListTitleBar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.mediquo_activity_allergies.*
import kotlinx.android.synthetic.main.mediquo_layout_medical_history_list_title_bar.*

/**
 * Created by Héctor Manrique on 4/15/21.
 */

class AllergiesActivity : TitleBarBaseActivity() {
    private lateinit var allergiesAdapter: AllergiesAdapter
    private var loading = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleBar(MedicalHistoryListTitleBar(this, getString(R.string.meetingdoctors_medical_history_allergies)))
        setContentView(R.layout.mediquo_activity_allergies)
        allergiesAdapter = AllergiesAdapter(this)
        listview?.adapter = allergiesAdapter
        listview?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            launchAllergy(this@AllergiesActivity, allergiesAdapter.getItem(position)!!.id)
        }
        add_button?.setOnClickListener { launchNewAllergy(this@AllergiesActivity) }
        add_button_empty?.setOnClickListener { launchNewAllergy(this@AllergiesActivity) }
    }

    override fun onReady() {
        super.onReady()
        refresh()
    }

    @SuppressLint("CheckResult")
    private fun refresh() {
        loading = true
        instance?.getMedicalHistoryRepository()
                ?.getAllergies()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ allergies ->
                    loading = false
                    if (allergies != null) {
                        if (allergies.isEmpty()) {
                            empty_layout?.visibility = View.VISIBLE
                            separator?.visibility = View.GONE
                            listview?.visibility = View.GONE
                        } else {
                            separator?.visibility = View.VISIBLE
                            listview?.visibility = View.VISIBLE
                            empty_layout?.visibility = View.GONE
                            allergiesAdapter.setAllergies(allergies)
                        }
                    }
                }) { throwable ->
                    loading = false
                    throwable?.printStackTrace()
                }
    }

    @SuppressLint("CheckResult")
    private fun getAllergies() {
        instance?.getMedicalHistoryRepository()
                ?.getAllergies()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ allergies ->
                    loading = false
                    if (allergies != null) {
                        if (allergies.isEmpty()) {
                            empty_layout?.visibility = View.VISIBLE
                            separator?.visibility = View.GONE
                            listview?.visibility = View.GONE
                        } else {
                            separator?.visibility = View.VISIBLE
                            listview?.visibility = View.VISIBLE
                            empty_layout?.visibility = View.GONE
                            allergiesAdapter.setAllergies(allergies)
                        }
                    }
                }) { throwable ->
                    loading = false
                    throwable?.printStackTrace()
                }
    }
}
