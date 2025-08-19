package com.ags.admin.ui.dashboardScreen

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ags.admin.R
import com.ags.admin.databinding.ItemUserBinding
import com.ags.admin.model.User
import com.bumptech.glide.Glide

class UserAdapter(
    private var users: List<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvName.text = user.name
            binding.tvEmail.text = user.email
            binding.tvRole.apply {
                text = user.role
                if (user.role == "admin") {
                    setTextColor(Color.RED)
                    setTypeface(null, Typeface.BOLD)
                } else {
                    setTextColor(textColors)
                }
            }


            Glide.with(binding.ivProfile.context)
                .load(user.profileUrl)
                .placeholder(R.drawable.ic_person)  // while loading
                .error(R.drawable.ic_person)  // if failed
                .into(binding.ivProfile)


            // Set click listener
            binding.root.setOnClickListener {
                onItemClick(user)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    fun updateList(newList: List<User>) {
        users = newList
        notifyDataSetChanged()
    }
}