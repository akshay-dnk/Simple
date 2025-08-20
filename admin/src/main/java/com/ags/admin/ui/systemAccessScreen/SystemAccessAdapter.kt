package com.ags.admin.ui.systemAccessScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ags.admin.databinding.ItemSystemAccessBinding
import com.ags.admin.model.SystemAccess

class SystemAccessAdapter(
    private var items: List<SystemAccess>,
    private val onItemClick: (SystemAccess) -> Unit
) : RecyclerView.Adapter<SystemAccessAdapter.SystemAccessViewHolder>() {

    inner class SystemAccessViewHolder(val binding: ItemSystemAccessBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SystemAccess) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description
            binding.ivIcon.setImageResource(item.iconRes)

            // Enable/disable card based on permission
            binding.root.isEnabled = item.enabled
            binding.root.alpha = if (item.enabled) 1.0f else 0.3f
            binding.root.strokeColor = if (item.enabled) {
                binding.root.context.getColor(android.R.color.holo_blue_light)
            } else {
                binding.root.context.getColor(android.R.color.darker_gray)
            }

            binding.root.setOnClickListener {
                if (item.enabled) {
                    onItemClick(item)
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SystemAccessViewHolder {
        val binding =
            ItemSystemAccessBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SystemAccessViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SystemAccessViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    // Call this when features list updates
    fun updateList(newItems: List<SystemAccess>) {
        items = newItems
        notifyDataSetChanged()
    }
}