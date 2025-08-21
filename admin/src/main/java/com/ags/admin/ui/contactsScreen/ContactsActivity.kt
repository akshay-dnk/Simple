package com.ags.admin.ui.contactsScreen

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ags.admin.databinding.ActivityContactsBinding
import com.ags.admin.model.User
import com.ags.core.BaseActivity
import com.ags.core.model.ContactInfo
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactsActivity : BaseActivity() {

    private lateinit var binding: ActivityContactsBinding
    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var adapter: ContactsAdapter
    private lateinit var user: User

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        user = intent.getParcelableExtra("user", User::class.java) ?: return finish()

        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarsPadding(binding.root)

        setupRecyclerView()
        observeContactsUiState()

        // Load all users’ contacts
        viewModel.loadUserContacts(user.email)
    }

    // Setup RecyclerView with adapter
    private fun setupRecyclerView() {
        adapter = ContactsAdapter()
        binding.rvContacts.layoutManager = LinearLayoutManager(this)
        binding.rvContacts.adapter = adapter
    }

    private fun observeContactsUiState() {
        lifecycleScope.launch {
            viewModel.contactsUiState.collectLatest { state ->
                when (state) {
                    is ContactsUiState.Loading -> showLoading()
                    is ContactsUiState.Success -> showContacts(state.contacts)
                    is ContactsUiState.Error -> showError(state.message)
                }
            }
        }
    }

    private fun showLoading() {
        binding.pbContacts.visibility = View.VISIBLE
        binding.rvContacts.visibility = View.GONE
        binding.tvEmptyMessage.visibility = View.GONE
    }

    private fun showContacts(contacts: List<ContactInfo>) {
        binding.pbContacts.visibility = View.GONE
        if (contacts.isEmpty()) {
            binding.tvEmptyMessage.visibility = View.VISIBLE
            binding.rvContacts.visibility = View.GONE
        } else {
            binding.tvEmptyMessage.visibility = View.GONE
            binding.rvContacts.visibility = View.VISIBLE
            adapter.submitList(contacts)
        }
    }

    private fun showError(message: String) {
        binding.pbContacts.visibility = View.GONE
        binding.rvContacts.visibility = View.GONE
        binding.tvEmptyMessage.text = message
        binding.tvEmptyMessage.visibility = View.VISIBLE
    }
}