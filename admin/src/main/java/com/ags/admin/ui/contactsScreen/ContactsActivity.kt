package com.ags.admin.ui.contactsScreen

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.ags.admin.databinding.ActivityContactsBinding
import com.ags.admin.model.User
import com.ags.core.BaseActivity

class ContactsActivity : BaseActivity() {

    private lateinit var binding: ActivityContactsBinding
    private val viewModel: ContactsViewModel by viewModels()
    private val adapter = ContactsAdapter()
    private lateinit var user: User

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        user = intent.getParcelableExtra("user", User::class.java) ?: return finish()

        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarsPadding(binding.root)

        binding.rvContacts.layoutManager = LinearLayoutManager(this)
        binding.rvContacts.adapter = adapter

        // Observe Firestore contacts
        viewModel.contacts.observe(this) { contacts ->
            adapter.submitList(contacts)
        }

        // Load all users’ contacts
        viewModel.loadUserContacts(user.email)
    }
}