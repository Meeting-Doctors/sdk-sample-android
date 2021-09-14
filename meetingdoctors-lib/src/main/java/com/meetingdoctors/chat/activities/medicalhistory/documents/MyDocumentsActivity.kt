package com.meetingdoctors.chat.activities.medicalhistory.documents

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.annotation.Keep
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.TitleBarBaseActivity
import com.meetingdoctors.chat.activities.medicalhistory.documents.adapter.MedicalHistoryOptionClickListener
import com.meetingdoctors.chat.activities.medicalhistory.documents.adapter.MedicalHistoryOptionsAdapter
import com.meetingdoctors.chat.activities.medicalhistory.prescription.MedicalPrescriptionActivity
import com.meetingdoctors.chat.activities.medicalhistory.utils.MedicalHistoryVisibilityOptions
import com.meetingdoctors.chat.activities.medicalhistory.utils.visibilityOptionsIntentExtra
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.domain.entities.MedicalHistoryOption
import com.meetingdoctors.chat.views.HomeTitleBar
import com.meetingdoctors.chat.views.MedicalHistoryListTitleBar
import kotlinx.android.synthetic.main.mediquo_activity_my_documents.*

class MyDocumentsActivity : TitleBarBaseActivity(), MedicalHistoryOptionClickListener {

    private var visibilityOptionsMap : Map<MedicalHistoryVisibilityOptions, Boolean> = HashMap()

    @Keep
    companion object {

        const val MEDICATION_ID = "medication_id"

        @JvmStatic
        fun newInstance(context: Context, visibilityOptionsMap :  Map<MedicalHistoryVisibilityOptions, Boolean>) {
            val intent = Intent(context, MyDocumentsActivity::class.java)
            intent.putExtra(MEDICATION_ID, 0L)
            val extras = Bundle()
            extras.putSerializable(visibilityOptionsIntentExtra, visibilityOptionsMap
                    as HashMap<MedicalHistoryVisibilityOptions, Boolean>)
            intent.putExtra(visibilityOptionsIntentExtra, visibilityOptionsMap)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }
    }

    private val myDocumentsViewModelFactory: MyDocumentsViewModelFactory by lazy {
        MyDocumentsViewModelFactory(Repository.instance!!,
                visibilityOptionsMap as HashMap<MedicalHistoryVisibilityOptions, Boolean>)
    }

    private val viewModel: MyDocumentsViewModel by lazy {
        ViewModelProvider(this,
                myDocumentsViewModelFactory).get(MyDocumentsViewModel::class.java)
    }

    lateinit var medicalHistoryOptionsAdapter: MedicalHistoryOptionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val homeTitleBar = MedicalHistoryListTitleBar(this, getString(R.string.meetingdoctors_medical_history_my_documents))
        homeTitleBar.hideAddButton()
        setTitleBar(homeTitleBar)

        setContentView(R.layout.mediquo_activity_my_documents)

        intent.extras?.let {
            visibilityOptionsMap = it.get(visibilityOptionsIntentExtra) as Map<MedicalHistoryVisibilityOptions, Boolean>
        }

        setUpRecyclerList()

        initObservers()

        viewModel.init()
    }

    private fun initObservers() {
        viewModel.medicalHistoryOptionsState.observe(this, Observer { state ->
            when (state) {
                is MedicalHistoryOptionsState.Success -> {
                    renderSuccessState(state.medicalHistoryOptionsList)
                }
            }
        })
    }

    private fun setUpRecyclerList() {

        medicalHistoryOptionsAdapter = MedicalHistoryOptionsAdapter()
        medicalHistoryOptionsAdapter.apply {
            clickListener = this@MyDocumentsActivity
        }
        val layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(
                this,
                layoutManager.orientation
        )
        dividerItemDecoration.setDrawable(ColorDrawable(resources.getColor(R.color.meetingdoctors_soft_gray)))
        documentOptionsList?.apply {
            this.layoutManager = layoutManager
            this.adapter = medicalHistoryOptionsAdapter
            this.addItemDecoration(dividerItemDecoration)
        }
    }

    private fun renderSuccessState(medicalHistoryOptionsList: List<MedicalHistoryOption>) {

        medicalHistoryOptionsAdapter.apply {
            this.medicalHistoryOptionsList = medicalHistoryOptionsList
            notifyDataSetChanged()
        }
    }

    override fun onClickOption(medicalHistoryOption: MedicalHistoryOption) {

        when (medicalHistoryOption) {
            MedicalHistoryOption.VIDEOCALL_REPORT -> {
                launchReports(this)
            }
            MedicalHistoryOption.DERIVATION -> {
                launchReferrals(this)
            }
            MedicalHistoryOption.PRESCRIPTION -> {
                MedicalPrescriptionActivity.newInstance(this)
            }
        }
    }
}