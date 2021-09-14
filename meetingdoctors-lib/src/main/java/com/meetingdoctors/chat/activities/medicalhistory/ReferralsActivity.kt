package com.meetingdoctors.chat.activities.medicalhistory

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.TitleBarBaseActivity
import com.meetingdoctors.chat.adapters.ReferralsAdapter
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.data.webservices.mappers.ReferralsDomainMapper
import com.meetingdoctors.chat.helpers.openFile
import com.meetingdoctors.chat.views.MedicalHistoryListTitleBar
import com.meetingdoctors.chat.views.extensions.OnItemClickListener
import com.meetingdoctors.chat.views.extensions.addOnItemClickListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.meetingdoctors_activity_documents.*

internal class ReferralsActivity: TitleBarBaseActivity(), OnItemClickListener {
    private lateinit var documentsAdapter: ReferralsAdapter
    private var loading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar()
        setContentView(R.layout.meetingdoctors_activity_documents)

        addItemDecorator()
        initRecyclerView()
    }

    private fun initToolbar() {
        val titleBar = MedicalHistoryListTitleBar(this, getString(R.string.meetingdoctors_medical_history_referrals))
        titleBar.hideAddButton()
        setTitleBar(titleBar)
    }

    private fun addItemDecorator() {
        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.list_separator)!!)
        documents_activity_recyclerview.addItemDecoration(divider)
    }

    private fun initRecyclerView() {
        documents_activity_recyclerview.apply {
            setHasFixedSize(true)
            documentsAdapter = ReferralsAdapter()
            adapter = documentsAdapter
            val documentsLayoutManager = LinearLayoutManager(this@ReferralsActivity)
            layoutManager = documentsLayoutManager
            addOnItemClickListener(this@ReferralsActivity)
        }
    }

    override fun onReady() {
        super.onReady()
        refresh()
    }

    @SuppressLint("CheckResult")
    private fun refresh() {
        loading = true
        documents_activity_empty_layout.visibility = View.VISIBLE
        documents_activity_loading_view.visibility = View.VISIBLE

        try{
            Repository.instance?.getMedicalHistoryRepository()?.getDocuments()
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe({ (referrals) ->
                        loading = false
                        documents_activity_loading_view.visibility = View.GONE;
                        if (referrals != null) {
                            if (referrals.isEmpty()) {
                                documents_activity_empty_layout.visibility = View.VISIBLE
                                documents_activity_separator.visibility = View.GONE
                                documents_activity_recyclerview!!.visibility = View.GONE
                            } else {
                                documents_activity_separator.visibility = View.VISIBLE
                                documents_activity_recyclerview!!.visibility = View.VISIBLE
                                documents_activity_empty_layout.visibility = View.GONE
                                documentsAdapter!!.submitList(ReferralsDomainMapper.mapFromRemoteArray(referrals!!))
                            }
                        }
                    }, { throwable ->
                        loading = false
                        documents_activity_loading_view.visibility = View.GONE
                        throwable?.printStackTrace()
                    })
        } catch (e: Exception) {
            Log.e("Referrals", "${e.message}")
        }
    }

    override fun onItemClick(position: Int, view: View) {
        val report = documentsAdapter!!.getItemAt(position)
        openFile(this@ReferralsActivity, report?.url, "${report?.filename}")
    }
}