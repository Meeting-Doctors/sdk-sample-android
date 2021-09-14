package com.meetingdoctors.chat.adapters

import androidx.recyclerview.widget.DiffUtil
import com.meetingdoctors.chat.domain.entities.Referral

internal class ReferralDiffUtilCallback: DiffUtil.ItemCallback<Referral>() {
    override fun areItemsTheSame(oldItem: Referral, newItem: Referral): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Referral, newItem: Referral): Boolean = oldItem == newItem

}