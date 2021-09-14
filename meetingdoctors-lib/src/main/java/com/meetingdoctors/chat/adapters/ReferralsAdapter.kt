package com.meetingdoctors.chat.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.domain.DATE_FORMAT
import com.meetingdoctors.chat.domain.entities.Referral
import com.meetingdoctors.chat.domain.entities.ReferralType
import com.meetingdoctors.chat.presentation.entitiesextensions.formatDate
import com.meetingdoctors.chat.presentation.entitiesextensions.parseDate
import kotlinx.android.synthetic.main.meetingdoctors_referral_item.view.*

internal class ReferralsAdapter: ListAdapter<Referral, ReferralsAdapter.ReportViewHolder>(ReferralDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder = ReportViewHolder(parent)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bindView(getItem(position), holder.itemView.context)
    }

    fun getItemAt(position:Int): Referral? = getItem(position)

    inner class ReportViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.meetingdoctors_referral_item, parent, false))
    {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun bindView(document: Referral, context: Context) {
            itemView.apply {
                document_item_professional_name?.text = document.professional?.name ?: ""
                document_item_category?.text = bindReferralTypeToUI(context, document.type)
                document_item_title?.text = document.friendlyName ?: ""
                document_item_date?.text = getParseDate(document.createdAt, context)
                document_item_icon?.setImageResource(R.drawable.mediquo_ic_medical_history_referral)
            }

        }

        private fun bindReferralTypeToUI(context: Context, referralType: ReferralType?): String {
            return when (referralType) {
                ReferralType.interconsultation -> context.getString(R.string.meetingdoctors_medical_history_referral_type_interconsultation)
                ReferralType.diagnostic_procedures -> context.getString(R.string.meetingdoctors_medical_history_referral_type_diagnostic_procedures)
                ReferralType.therapeutic_procedures -> context.getString(R.string.meetingdoctors_medical_history_referral_type_therapeutical_procedures)
                ReferralType.undefined -> context.getString(R.string.meetingdoctors_medical_history_referral_type_undefined)
                else -> context.getString(R.string.meetingdoctors_medical_history_referral_type_undefined)
            }
        }

        private fun getParseDate(date: String?, context: Context): String? {
            val calendarWithDateFormatter = date.parseDate(DATE_FORMAT_DD_MM_YYYY)

//            // dd 'of' MMMM 'of' YYYY
//            val datePresentationFormat =
//                    DATE_FORMAT_DD_MMMM_YYYY.replace("/", "-")
            return date.formatDate(DATE_FORMAT, DATE_FORMAT_DD_MM_YYYY)
            //return formatDate(calendarWithDateFormatter, DATE_FORMAT_DD_MMMM_YYYY) ?: "Fecha no disponible"
        }
    }
}

private const val DATE_FORMAT_DD_MM_YYYY = "dd-MM-yyyy"