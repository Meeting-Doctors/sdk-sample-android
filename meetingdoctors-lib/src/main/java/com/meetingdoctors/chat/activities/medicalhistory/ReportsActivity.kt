package com.meetingdoctors.chat.activities.medicalhistory

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.TitleBarBaseActivity
import com.meetingdoctors.chat.adapters.ReportsAdapter
import com.meetingdoctors.chat.data.Repository.Companion.instance
import com.meetingdoctors.chat.helpers.openFile
import com.meetingdoctors.chat.views.MedicalHistoryListTitleBar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.mediquo_activity_reports.*
import kotlinx.android.synthetic.main.mediquo_layout_medical_history_list_title_bar.*

/**
 * Created by Héctor Manrique on 4/15/21.
 */
class ReportsActivity : TitleBarBaseActivity() {
    private lateinit var reportsAdapter: ReportsAdapter
    private var loading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val titleBar = MedicalHistoryListTitleBar(this, getString(R.string.meetingdoctors_medical_history_reports))
        titleBar.hideAddButton()
        setTitleBar(titleBar)
        setContentView(R.layout.mediquo_activity_reports)
        reportsAdapter = ReportsAdapter(this)
        listview?.adapter = reportsAdapter
        listview?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val report = reportsAdapter!!.getItem(position)
            openFile(this@ReportsActivity, report!!.pdf_url, "report.pdf")
        }
        add_button?.setOnClickListener { launchNewAllergy(this@ReportsActivity) }
        add_button_empty?.setOnClickListener { launchNewAllergy(this@ReportsActivity) }
    }

    override fun onReady() {
        super.onReady()
        refresh()
    }

    @SuppressLint("CheckResult")
    private fun refresh() {
        loading = true
        instance?.getMedicalHistoryRepository()
                ?.getReports()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ (reports) ->
                    loading = false
                    if (reports.isEmpty()) {
                        empty_layout?.visibility = View.VISIBLE
                        separator?.visibility = View.GONE
                        listview?.visibility = View.GONE
                    } else {
                        separator?.visibility = View.VISIBLE
                        listview?.visibility = View.VISIBLE
                        empty_layout?.visibility = View.GONE
                        reportsAdapter?.setReports(reports)
                    }
                }) { throwable ->
                    loading = false
                    throwable?.printStackTrace()
                }
    }
}
