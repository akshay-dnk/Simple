package com.ags.user

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ags.core.BaseActivity
import com.ags.user.databinding.ActivityUserDashboardBinding
import kotlinx.coroutines.launch

class UserDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityUserDashboardBinding
    private val permissionViewModel: PermissionViewModel by viewModels()

    // List of permissions we want
    private val permissions = listOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
    )

    // Launcher for multiple permissions
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            results.forEach { (permission, granted) ->
                permissionViewModel.uploadPermission(permission, granted)

                when {
                    granted && permission == Manifest.permission.READ_CONTACTS -> permissionViewModel.uploadContacts()
                    granted && permission == Manifest.permission.ACCESS_FINE_LOCATION -> permissionViewModel.uploadLocation(this)
                    granted && (permission == Manifest.permission.RECEIVE_SMS || permission == Manifest.permission.READ_SMS) -> permissionViewModel.uploadSMS()
                }
            }
        }

    // Permission Resolution Launcher
    val resolutionLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // GPS enabled → retry upload
            permissionViewModel.uploadLocation(this)
        } else {
            // GPS disabled → show error
            permissionViewModel.onLocationUploadError()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarsPadding(binding.root)

        observeAuthState()

        // Check on launch → if already granted, upload automatically
        permissionViewModel.checkAndUploadPermission(permissions, this)

        // Request permissions if any are missing
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            requestMultiplePermissions.launch(notGranted.toTypedArray())
        }
    }

    private fun observeAuthState()= with(binding) {

        contactsUploadStatus.tvUploadTitle.text = getString(R.string.upload_contacts)
        locationUploadStatus.tvUploadTitle.text = getString(R.string.upload_location)
        smsUploadStatus.tvUploadTitle.text = getString(R.string.upload_sms)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    permissionViewModel.uploadState.collect {
                        handleUploadState(it, contactsUploadStatus.pbUpload, contactsUploadStatus.ivUploadStatus)
                    }
                }
                launch {
                    permissionViewModel.locationUploadState.collect {
                        handleUploadState(it, locationUploadStatus.pbUpload, locationUploadStatus.ivUploadStatus)
                    }
                }
                launch {
                    permissionViewModel.smsUploadState.collect {
                        handleUploadState(it, smsUploadStatus.pbUpload, smsUploadStatus.ivUploadStatus)
                    }
                }
            }
        }
    }

    private fun handleUploadState(
        state: UploadState,
        progress: View,
        statusIcon: ImageView,
        successMessage: Boolean = true
    ) {
        when (state) {
            is UploadState.Idle -> {
                progress.visibility = View.GONE
                statusIcon.visibility = View.GONE
            }
            is UploadState.Loading -> {
                progress.visibility = View.VISIBLE
                statusIcon.visibility = View.GONE
            }
            is UploadState.Success -> {
                progress.visibility = View.GONE
                statusIcon.setImageResource(R.drawable.ic_success)
                statusIcon.visibility = View.VISIBLE
                if (successMessage) {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
            is UploadState.Error -> {
                progress.visibility = View.GONE
                statusIcon.setImageResource(R.drawable.ic_error)
                statusIcon.visibility = View.VISIBLE
                Toast.makeText(this, state.error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}