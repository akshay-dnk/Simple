package com.ags.admin.ui.smsScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ags.admin.databinding.ItemSmsBinding
import com.ags.admin.utils.AppUtils
import com.ags.core.model.SMSInfo

class SMSAdapter : ListAdapter<SMSInfo, SMSAdapter.SMSViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<SMSInfo>() {
        override fun areItemsTheSame(oldItem: SMSInfo, newItem: SMSInfo): Boolean =
            oldItem.date == newItem.date

        override fun areContentsTheSame(oldItem: SMSInfo, newItem: SMSInfo): Boolean =
            oldItem == newItem
    }

    inner class SMSViewHolder(private val binding: ItemSmsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sms: SMSInfo) {
            binding.tvAddress.text = sms.address ?: "N/A"
            binding.tvBody.text = sms.body ?: "N/A"
            binding.tvDate.text = AppUtils.formatTimestamp(sms.date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SMSViewHolder {
        val binding = ItemSmsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SMSViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SMSViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}