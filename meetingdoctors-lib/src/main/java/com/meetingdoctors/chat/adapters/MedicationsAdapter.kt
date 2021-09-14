package com.meetingdoctors.chat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.domain.entities.Medication
import java.util.*

/**
 * Created by Héctor Manrique on 4/12/21.
 */

class MedicationsAdapter(context: Context) : BaseAdapter() {
    private val context: Context = context
    private val medications: ArrayList<Medication> = ArrayList()

    fun setMedications(medications: List<Medication>) {
        this.medications.apply {
            clear()
            addAll(medications)
        }
        notifyDataSetChanged()
    }

    inner class ServiceViewHolder {
        var name: TextView? = null
        var posology: TextView? = null
    }

    override fun getCount(): Int {
        return medications.size
    }

    override fun getItem(position: Int): Medication? {
        return medications[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View? {
        var view = view
        val holder: ServiceViewHolder
        if (view == null) {
            holder = ServiceViewHolder()
            view = LayoutInflater.from(context).inflate(R.layout.mediquo_item_medical_history_medication, parent, false)
            holder.name = view.findViewById<View>(R.id.name) as TextView
            holder.posology = view.findViewById<View>(R.id.posology) as TextView
            view.tag = holder
        } else {
            holder = view.tag as ServiceViewHolder
        }
        val (_, name, posology) = medications[position]
        holder.name!!.text = name
        holder.posology!!.text = posology
        return view
    }

}
