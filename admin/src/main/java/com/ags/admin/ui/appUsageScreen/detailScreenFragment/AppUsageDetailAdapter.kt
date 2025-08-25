package com.ags.admin.ui.appUsageScreen.detailScreenFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ags.admin.databinding.ItemAppUsageBinding
import com.ags.admin.utils.AppUtils
import com.ags.core.model.AppUsageInfo

class AppUsageDetailAdapter : ListAdapter<AppUsageInfo, AppUsageDetailAdapter.AppUsageDetailViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<AppUsageInfo>() {
        override fun areItemsTheSame(oldItem: AppUsageInfo, newItem: AppUsageInfo) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: AppUsageInfo, newItem: AppUsageInfo) =
            oldItem == newItem
    }

    inner class AppUsageDetailViewHolder(private val binding: ItemAppUsageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(appUsageInfo: AppUsageInfo) {
            binding.tvAppName.text = appUsageInfo.appName
            binding.tvAppUsage.text = AppUtils.convertTimeMillisToHours(appUsageInfo.totalTimeVisible)
            binding.tvLastTimeUsed.text = AppUtils.formatTimestamp(appUsageInfo.lastTimeUsed)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppUsageDetailViewHolder {
        val binding = ItemAppUsageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppUsageDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppUsageDetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}