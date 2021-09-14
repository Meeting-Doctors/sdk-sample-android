package com.meetingdoctors.chat.activities.medicalhistory.documents.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.meetingdoctors.chat.domain.entities.MedicalHistoryOption
import kotlinx.android.synthetic.main.mediquo_item_medical_history.view.*

class MedicalHistoryOptionViewHolder(view: View): RecyclerView.ViewHolder(view) {

    fun bind(medicalHistoryOption: MedicalHistoryOption, clickListener: MedicalHistoryOptionClickListener) {
        itemView.apply {
            name.text = this.context.getString(medicalHistoryOption.optionName)
            image.setImageResource(medicalHistoryOption.imageResource)
            setOnClickListener {
                clickListener.onClickOption(medicalHistoryOption)
            }
        }
    }
}
