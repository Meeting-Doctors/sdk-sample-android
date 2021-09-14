package com.meetingdoctors.chat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.domain.DATE_FORMAT
import com.meetingdoctors.chat.domain.entities.Report
import com.meetingdoctors.chat.presentation.entitiesextensions.formatDate
import java.util.*


/**
 * Created by Héctor Manrique on 4/12/21.
 */

class ReportsAdapter(context: Context) : BaseAdapter() {
    private val context: Context = context
    private var reports: ArrayList<Report> = ArrayList()

    fun setReports(reports: List<Report>) {
        this.reports.apply {
            clear()
            addAll(reports)
        }
        notifyDataSetChanged()
    }

    inner class ServiceViewHolder {
        var reportDate: TextView? = null
        var professionalAvatar: ImageView? = null
        var professionalName: TextView? = null
    }

    override fun getCount(): Int {
        return reports.size
    }

    override fun getItem(position: Int): Report? {
        return reports[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View? {
        var view = view
        val holder: ServiceViewHolder
        if (view == null) {
            holder = ServiceViewHolder()
            view = LayoutInflater.from(context).inflate(R.layout.mediquo_item_medical_history_report, parent, false)
            holder.apply {
                reportDate = view.findViewById(R.id.report_date)
                professionalAvatar = view.findViewById(R.id.avatar)
                professionalName = view.findViewById(R.id.professional_name)
            }
            view.tag = holder
        } else {
            holder = view.tag as ServiceViewHolder
        }
        val (_, _, maker, created_at) = reports[position]
        if (maker.avatar?.isNotEmpty() == true) {
            if (holder.professionalAvatar != null) {
                Glide.with(context)
                        .load(maker.avatar)
                        .apply(RequestOptions.circleCropTransform())
                        .into(holder.professionalAvatar!!)
                holder.professionalAvatar?.visibility = View.VISIBLE

            } else {
                holder.professionalAvatar?.visibility = View.INVISIBLE
            }

        } else {
            holder.professionalAvatar?.visibility = View.INVISIBLE
        }
        val date = created_at.formatDate(DATE_FORMAT, "dd-MM-yy")
        val time = created_at.formatDate(DATE_FORMAT, "HH:mm")
        holder.reportDate!!.text = context.getString(R.string.meetingdoctors_medical_history_report_date, date, time)
        holder.professionalName!!.text = "${maker.name} · ${maker.description}"
        return view
    }

}
