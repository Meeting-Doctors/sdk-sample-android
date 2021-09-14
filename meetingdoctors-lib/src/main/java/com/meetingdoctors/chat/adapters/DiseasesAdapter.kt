package com.meetingdoctors.chat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.domain.entities.Disease
import java.util.*

/**
 * Created by Héctor Manrique on 4/12/21.
 */
class DiseasesAdapter(context: Context) : BaseAdapter() {
    private val context: Context = context
    private val diseases: ArrayList<Disease> = ArrayList()
    fun setDiseases(diseases: List<Disease>) {
        this.diseases.apply {
            clear()
            addAll(diseases)
        }
        notifyDataSetChanged()
    }

    inner class ServiceViewHolder {
        var name: TextView? = null
    }

    override fun getCount(): Int {
        return diseases.size
    }

    override fun getItem(position: Int): Disease? {
        return diseases[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View? {
        var view = view
        val holder: ServiceViewHolder
        if (view == null) {
            holder = ServiceViewHolder()
            view = LayoutInflater.from(context).inflate(R.layout.mediquo_item_medical_history_disease, parent, false)
            holder.name = view.findViewById<View>(R.id.name) as TextView
            view.tag = holder
        } else {
            holder = view.tag as ServiceViewHolder
        }
        val (_, name) = diseases[position]
        holder.name?.text = name
        return view
    }

}
