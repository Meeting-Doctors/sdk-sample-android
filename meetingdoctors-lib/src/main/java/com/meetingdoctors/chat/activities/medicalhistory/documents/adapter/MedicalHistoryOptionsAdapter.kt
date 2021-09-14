package com.meetingdoctors.chat.activities.medicalhistory.documents.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.domain.entities.MedicalHistoryOption

class MedicalHistoryOptionsAdapter(): RecyclerView.Adapter<MedicalHistoryOptionViewHolder>() {

    var medicalHistoryOptionsList: List<MedicalHistoryOption> = emptyList()

    lateinit var clickListener: MedicalHistoryOptionClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicalHistoryOptionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mediquo_item_medical_history, parent, false)
        return MedicalHistoryOptionViewHolder(view)
    }

    override fun getItemCount(): Int = medicalHistoryOptionsList.size

    override fun onBindViewHolder(holder: MedicalHistoryOptionViewHolder, position: Int) {
        holder.bind(medicalHistoryOptionsList[position], clickListener)
    }
}