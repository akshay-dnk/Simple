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
import androidx.lifecycle.lifecycleScope
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
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Launcher for multiple permissions
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            results.forEach { (permission, granted) ->
                permissionViewModel.uploadPermission(permission, granted)

                if (granted && permission == Manifest.permission.READ_CONTACTS) {
                    permissionViewModel.uploadContacts()
                }

                if (granted && permission == Manifest.permission.ACCESS_FINE_LOCATION) {
                    permissionViewModel.uploadLocation(this)
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

    private fun observeAuthState() {

        binding.locationUploadStatus.tvUploadTitle.text = "Upload Location"

        lifecycleScope.launch {
            permissionViewModel.uploadState.collect { state ->
                when (state) {
                    is UploadState.Idle -> {
                        binding.pbUploadContacts.visibility = View.GONE
                        binding.ivUploadStatus.visibility = View.GONE
                    }

                    is UploadState.Loading -> {
                        binding.pbUploadContacts.visibility = View.VISIBLE
                        binding.ivUploadStatus.visibility = View.GONE
                    }

                    is UploadState.Success -> {
                        binding.pbUploadContacts.visibility = View.GONE
                        binding.ivUploadStatus.apply {
                            visibility = View.VISIBLE
                            setImageResource(R.drawable.ic_success)
                        }
                        Toast.makeText(this@UserDashboardActivity, state.message, Toast.LENGTH_SHORT).show()
                    }

                    is UploadState.Error -> {
                        binding.pbUploadContacts.visibility = View.GONE
                        binding.ivUploadStatus.apply {
                            visibility = View.VISIBLE
                            setImageResource(R.drawable.ic_error)
                        }
                        Toast.makeText(this@UserDashboardActivity, state.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Location upload state
        lifecycleScope.launch {
            permissionViewModel.locationUploadState.collect { state ->
                handleUploadState(state, binding.locationUploadStatus.pbUpload, binding.locationUploadStatus.ivUploadStatus)
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