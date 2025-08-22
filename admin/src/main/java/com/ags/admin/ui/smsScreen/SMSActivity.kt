package com.ags.admin.ui.smsScreen

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ags.admin.databinding.ActivitySmsactivityBinding
import com.ags.admin.model.User
import com.ags.core.BaseActivity
import com.ags.core.model.SMSInfo
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SMSActivity : BaseActivity() {

    private lateinit var binding: ActivitySmsactivityBinding
    private val viewModel: SMSViewModel by viewModels()
    private lateinit var adapter: SMSAdapter
    private lateinit var user: User


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        user = intent.getParcelableExtra("user", User::class.java) ?: return finish()

        binding = ActivitySmsactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarsPadding(binding.root)

        setupRecyclerView()
        observeSMSUiState()

        // Load all user’s SMS
        viewModel.loadUserSMS(user.email)
    }

    // Setup RecyclerView with adapter
    private fun setupRecyclerView() {
        adapter = SMSAdapter()
        binding.rvSMS.layoutManager = LinearLayoutManager(this)
        binding.rvSMS.adapter = adapter
    }

    // Observe SMS UI state
    private fun observeSMSUiState() {
        lifecycleScope.launch {
            viewModel.smsUiState.collectLatest { state ->
                when (state) {
                    is SMSUiState.Loading -> showLoading()
                    is SMSUiState.Success -> showSMS(state.smsList)
                    is SMSUiState.Error -> showError(state.message)
                }
            }

        }
    }

    private fun showLoading() {
        binding.pbSMS.visibility = View.VISIBLE
        binding.rvSMS.visibility = View.GONE
        binding.tvEmptyMessage.visibility = View.GONE
    }

    private fun showSMS(smsList: List<SMSInfo>) {
        binding.pbSMS.visibility = View.GONE
        if (smsList.isEmpty()) {
            binding.tvEmptyMessage.visibility = View.VISIBLE
            binding.rvSMS.visibility = View.GONE
        } else {
            binding.tvEmptyMessage.visibility = View.GONE
            binding.rvSMS.visibility = View.VISIBLE
            adapter.submitList(smsList)
        }
    }

    private fun showError(message: String) {
        binding.pbSMS.visibility = View.GONE
        binding.rvSMS.visibility = View.GONE
        binding.tvEmptyMessage.text = message
        binding.tvEmptyMessage.visibility = View.VISIBLE
    }
}