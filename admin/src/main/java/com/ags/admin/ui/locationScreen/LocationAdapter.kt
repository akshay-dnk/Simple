package com.ags.admin.ui.locationScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ags.admin.databinding.ItemLocationBinding
import com.ags.core.model.LocationData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationAdapter(
    private val onItemClick: (LocationData) -> Unit
) : ListAdapter<LocationData, LocationAdapter.LocationViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<LocationData>() {
        override fun areItemsTheSame(oldItem: LocationData, newItem: LocationData): Boolean {
            return oldItem.timestamp == newItem.timestamp // unique per record
        }

        override fun areContentsTheSame(oldItem: LocationData, newItem: LocationData): Boolean {
            return oldItem == newItem
        }
    }

    inner class LocationViewHolder(private val binding: ItemLocationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(location: LocationData) {
            binding.tvLocation.text = location.address

            // Convert timestamp to readable format
            binding.tvTime.text = formatTimestamp(location.timestamp)

            binding.root.setOnClickListener { onItemClick(location) }
        }

        private fun formatTimestamp(timestamp: Long?): String {
            return if (timestamp != null) {
                val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                sdf.format(Date(timestamp))
            } else {
                "Unknown time"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}