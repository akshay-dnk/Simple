package com.ags.admin.ui.contactsScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ags.admin.databinding.ItemContactBinding
import com.ags.core.model.ContactInfo

class ContactsAdapter : ListAdapter<ContactInfo, ContactsAdapter.ContactViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<ContactInfo>() {
        override fun areItemsTheSame(oldItem: ContactInfo, newItem: ContactInfo): Boolean =
            oldItem.phone == newItem.phone && oldItem.userId == newItem.userId

        override fun areContentsTheSame(oldItem: ContactInfo, newItem: ContactInfo): Boolean =
            oldItem == newItem
    }

    inner class ContactViewHolder(private val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: ContactInfo) {
            binding.tvName.text = contact.name ?: "Unknown"
            binding.tvPhone.text = contact.phone ?: "N/A"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}