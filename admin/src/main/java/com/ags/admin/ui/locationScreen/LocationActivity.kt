package com.ags.admin.ui.locationScreen

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ags.admin.databinding.ActivityLocationBinding
import com.ags.admin.model.User
import com.ags.core.BaseActivity
import com.ags.core.model.LocationData
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LocationActivity : BaseActivity() {

    private lateinit var binding: ActivityLocationBinding
    private val viewModel: LocationViewModel by viewModels()
    private lateinit var adapter: LocationAdapter
    private lateinit var user: User

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        user = intent.getParcelableExtra("user", User::class.java) ?: return finish()

        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarsPadding(binding.root)

        setupRecyclerView()
        observeLocationUiState()

        viewModel.loadUserLocations(user.email)
    }

    // Setup RecyclerView with adapter
    private fun setupRecyclerView() {
        adapter = LocationAdapter { location -> onLocationClicked(location) }
        binding.rvLocation.layoutManager = LinearLayoutManager(this)
        binding.rvLocation.adapter = adapter
    }

    private fun observeLocationUiState() {
        lifecycleScope.launch {
            viewModel.locationUiState.collectLatest { state ->
                when (state) {
                    is LocationUiState.Loading -> showLoading()
                    is LocationUiState.Success -> showLocations(state.locations)
                    is LocationUiState.Error -> showError(state.message)
                }
            }
        }
    }

    private fun showLoading() {
        binding.pbLocation.visibility = View.VISIBLE
        binding.rvLocation.visibility = View.GONE
        binding.tvEmptyMessage.visibility = View.GONE
    }

    private fun showLocations(locations: List<LocationData>) {
        binding.pbLocation.visibility = View.GONE
        if (locations.isEmpty()) {
            binding.tvEmptyMessage.visibility = View.VISIBLE
            binding.rvLocation.visibility = View.GONE
        } else {
            binding.tvEmptyMessage.visibility = View.GONE
            binding.rvLocation.visibility = View.VISIBLE
            adapter.submitList(locations)
        }
    }

    private fun showError(message: String) {
        binding.pbLocation.visibility = View.GONE
        binding.rvLocation.visibility = View.GONE
        binding.tvEmptyMessage.text = message
        binding.tvEmptyMessage.visibility = View.VISIBLE
    }

    // Handle item clicks based on location selected
    private fun onLocationClicked(location: LocationData) {
        val lat = location.latitude
        val lng = location.longitude
        val address = location.address

        // Open Google Maps with selected location
        val gmmIntentUri = "geo:$lat,$lng?q=$lat,$lng($address)".toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        // If Google Maps not installed, fallback to browser
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                "https://maps.google.com/?q=$lat,$lng".toUri()
            )
            startActivity(browserIntent)
        }
    }
}