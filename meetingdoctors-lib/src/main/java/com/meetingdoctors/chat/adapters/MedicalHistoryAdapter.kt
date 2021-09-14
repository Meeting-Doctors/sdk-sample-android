package com.meetingdoctors.chat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.meetingdoctors.chat.R

/**
 * Created by Héctor Manrique on 4/12/21.
 */
//TODO: Se ha eliminado del constructor el map de medicalHistoryVisibilityOptions ya que no se estaba usando
class MedicalHistoryAdapter(context: Context)
    : BaseAdapter() {

    private val context: Context = context

    inner class ServiceViewHolder {
        var container: View? = null
        var image: ImageView? = null
        var name: TextView? = null
    }

    override fun getCount(): Int {
        return 4
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View? {
        var view = view
        val holder: ServiceViewHolder
        if (view == null) {
            holder = ServiceViewHolder()
            view = LayoutInflater.from(context).inflate(R.layout.mediquo_item_medical_history, parent, false)
            holder.apply {
                container = view.findViewById(R.id.item_medical_history_container)
                name = view.findViewById(R.id.name)
                image = view.findViewById(R.id.image)
            }
            view.tag = holder
        } else {
            holder = view.tag as ServiceViewHolder
        }
        when (position) {
            0 -> {
                holder.name?.setText(R.string.meetingdoctors_medical_history_allergies)
                holder.image?.setImageResource(R.drawable.mediquo_ic_medical_history_allergies)
            }
            1 -> {
                holder.name?.setText(R.string.meetingdoctors_medical_history_diseases)
                holder.image?.setImageResource(R.drawable.mediquo_ic_medical_history_diseases)
            }
            2 -> {
                holder.name?.setText(R.string.meetingdoctors_medical_history_medications)
                holder.image?.setImageResource(R.drawable.mediquo_ic_medical_history_medications)
            }
            3 -> {
                holder.name?.setText(R.string.meetingdoctors_medical_history_my_documents)
                holder.image?.setImageResource(R.drawable.mediquo_ic_medical_history_documents)
            }
        }
        return view
    }

}
