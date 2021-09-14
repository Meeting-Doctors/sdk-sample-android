package com.meetingdoctors.chat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.domain.entities.Allergy
import java.util.*

/**
 * Created by Héctor Manrique on 4/12/21.
 */
class AllergiesAdapter(context: Context) : BaseAdapter() {
    private val context: Context = context
    private var allergies: ArrayList<Allergy> = ArrayList()

    fun setAllergies(allergies: List<Allergy>) {
        this.allergies.apply {
            clear()
            addAll(allergies)
        }
        notifyDataSetChanged()
    }

    inner class ServiceViewHolder {
        var name: TextView? = null
        var severity1: ImageView? = null
        var severity2: ImageView? = null
        var severity3: ImageView? = null
        var severity4: ImageView? = null
        var severity5: ImageView? = null
    }

    override fun getCount(): Int {
        return allergies.size
    }

    override fun getItem(position: Int): Allergy? {
        return allergies[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View? {
        var view = view
        val holder: ServiceViewHolder
        if (view == null) {
            holder = ServiceViewHolder()
            view = LayoutInflater.from(context).inflate(R.layout.mediquo_item_medical_history_allergy, parent, false)
            holder.apply {
                name = view.findViewById<View>(R.id.name) as TextView
                severity1 = view.findViewById<View>(R.id.severity_1) as ImageView
                severity2 = view.findViewById<View>(R.id.severity_2) as ImageView
                severity3 = view.findViewById<View>(R.id.severity_3) as ImageView
                severity4 = view.findViewById<View>(R.id.severity_4) as ImageView
                severity5 = view.findViewById<View>(R.id.severity_5) as ImageView
            }

            view.tag = holder
        } else {
            holder = view.tag as ServiceViewHolder
        }
        val (_, allergyName, severity) = allergies[position]
        holder.name?.text = allergyName
        setSeverity(holder, severity.toInt())

        return view
    }

    //TODO: check new implementation
    private fun setSeverity(holder: ServiceViewHolder, value: Int) {

        //Set default gray color
        holder.apply {
            severity1?.setImageResource(R.drawable.mediquo_circle_gray)
            severity2?.setImageResource(R.drawable.mediquo_circle_gray)
            severity3?.setImageResource(R.drawable.mediquo_circle_gray)
            severity4?.setImageResource(R.drawable.mediquo_circle_gray)
            severity5?.setImageResource(R.drawable.mediquo_circle_gray)
        }
        //set proper severity
        for (i in 1..value) {
            when (i) {
                1 -> {
                    holder.severity1?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                }
                2 -> {
                    holder.severity2?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                }
                3 -> {
                    holder.severity3?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                }
                4 -> {
                    holder.severity4?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                }
                else -> {
                    holder.severity5?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                }
            }
        }
    }

}
