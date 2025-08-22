package com.ags.admin.ui.systemAccessScreen

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ags.admin.databinding.ActivitySystemAccessBinding
import com.ags.admin.model.SystemAccess
import com.ags.admin.model.SystemFeatureType
import com.ags.admin.model.User
import com.ags.admin.ui.contactsScreen.ContactsActivity
import com.ags.admin.ui.locationScreen.LocationActivity
import com.ags.admin.ui.smsScreen.SMSActivity
import com.ags.core.BaseActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SystemAccessActivity : BaseActivity() {

    private lateinit var binding: ActivitySystemAccessBinding
    private val viewModel: SystemAccessViewModel by viewModels()
    private lateinit var adapter: SystemAccessAdapter
    private lateinit var user: User

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!initUserFromIntent()) return

        binding = ActivitySystemAccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarsPadding(binding.root)


        setupRecyclerView()
        observeFeatures()

        // Load permissions for this user
        viewModel.loadUserPermissions(user.email)
    }

    // Safely get User from Intent
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initUserFromIntent(): Boolean {
        user = intent.getParcelableExtra("user", User::class.java) ?: run {
            finish()
            return false
        }
        return true
    }

    // Setup RecyclerView with adapter
    private fun setupRecyclerView() {
        adapter = SystemAccessAdapter(emptyList()) { feature -> onFeatureClicked(feature) }
        binding.rvSystemAccess.layoutManager = LinearLayoutManager(this)
        binding.rvSystemAccess.adapter = adapter
    }

    // Observe ViewModel changes
    private fun observeFeatures() {
        lifecycleScope.launch {
            viewModel.features.collectLatest { list ->
                adapter.updateList(list)
            }
        }
    }

    // Handle item clicks based on feature type
    private fun onFeatureClicked(feature: SystemAccess) {
        if (!feature.enabled) return

        when (feature.type) {
            SystemFeatureType.READ_CONTACTS -> openContactsActivity()
            SystemFeatureType.LIVE_CAMERA -> showToast("Camera feature coming soon")
            SystemFeatureType.FINE_LOCATION -> openLocationActivity()
            SystemFeatureType.RECORD_AUDIO -> showToast("Audio feature coming soon")
            SystemFeatureType.READ_SMS -> openSMSActivity()
        }
    }

    // Start ContactsActivity
    private fun openContactsActivity() {
        val intent = Intent(this, ContactsActivity::class.java).apply {
            putExtra("user", user)
        }
        startActivity(intent)
    }

    // Start LocationActivity
    private fun openLocationActivity() {
        val intent = Intent(this, LocationActivity::class.java).apply {
            putExtra("user", user)
        }
        startActivity(intent)
    }

    // Start SMSActivity
    private fun openSMSActivity() {
        val intent = Intent(this, SMSActivity::class.java).apply {
            putExtra("user", user)
        }
        startActivity(intent)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}