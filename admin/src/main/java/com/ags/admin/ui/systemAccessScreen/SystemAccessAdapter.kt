package com.ags.admin.ui.systemAccessScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ags.admin.databinding.ItemSystemAccessBinding
import com.ags.admin.model.SystemAccess

class SystemAccessAdapter(
    private val items: List<SystemAccess>,
    private val onItemClick: (SystemAccess) -> Unit
) : RecyclerView.Adapter<SystemAccessAdapter.SystemAccessViewHolder>() {

    inner class SystemAccessViewHolder(val binding: ItemSystemAccessBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SystemAccess) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description
            binding.ivIcon.setImageResource(item.iconRes)

            binding.root.setOnClickListener { onItemClick(item) }
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
}