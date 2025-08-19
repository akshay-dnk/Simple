package com.ags.admin.ui.dashboardScreen

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ags.admin.databinding.ActivityAdminDashboardBinding
import com.ags.core.BaseActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private val viewModel: AdminDashboardViewModel by viewModels()
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarsPadding(binding.root)

        adapter = UserAdapter(emptyList()) {
            // Handle item click
            Toast.makeText(this, "Clicked on ${it.email}", Toast.LENGTH_SHORT).show()
        }
        binding.rvUsers.adapter = adapter
        binding.rvUsers.layoutManager = LinearLayoutManager(this)

        // Observe users
        lifecycleScope.launch {
            viewModel.users.collectLatest { list ->
                adapter.updateList(list)
            }
        }
    }
}