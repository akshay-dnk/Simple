package com.ags.admin.ui.appUsageScreen.dateScreenFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ags.admin.databinding.ItemDateBinding
import com.ags.admin.utils.AppUtils
import com.ags.core.model.AppUsageDateInfo

class AppUsageDateAdapter(
    private val onDateClick: (String) -> Unit
) : ListAdapter<AppUsageDateInfo, AppUsageDateAdapter.DateViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<AppUsageDateInfo>() {
        override fun areItemsTheSame(oldItem: AppUsageDateInfo, newItem: AppUsageDateInfo) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: AppUsageDateInfo, newItem: AppUsageDateInfo) =
            oldItem == newItem
    }

    inner class DateViewHolder(private val binding: ItemDateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: AppUsageDateInfo) {
            binding.tvDate.text = date.date
            binding.tvTotalApps.text = date.totalApps.toString()
            binding.tvTotalUsageTime.text = AppUtils.convertTimeMillisToHours(date.totalUsageTime)
            binding.tvUpdatedAt.text = AppUtils.formatTimestamp(date.timestamp)

            binding.root.setOnClickListener { onDateClick(date.date) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val binding = ItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}