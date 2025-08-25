package com.ags.admin.ui.appUsageScreen.dateScreenFragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ags.admin.R
import com.ags.admin.databinding.FragmentAppUsageDateBinding
import com.ags.admin.model.User
import com.ags.admin.ui.appUsageScreen.AppUsageViewModel
import com.ags.admin.ui.appUsageScreen.detailScreenFragment.AppUsageDetailFragment
import com.ags.core.model.AppUsageDateInfo
import kotlinx.coroutines.launch

class AppUsageDateFragment : Fragment() {

    private var _binding: FragmentAppUsageDateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppUsageViewModel by viewModels()
    private lateinit var adapter: AppUsageDateAdapter
    private lateinit var user: User


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAppUsageDateBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user = requireActivity().intent.getParcelableExtra("user", User::class.java)!!

        setupRecyclerView()
        observeAppUsageDateUiState()

        viewModel.loadAvailableDates(user.email)

    }

    private fun setupRecyclerView() {
        adapter = AppUsageDateAdapter { date -> onDateClicked(date) }
        binding.rvDate.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDate.adapter = adapter
    }

    private fun onDateClicked(date: String) {
        val fragment = AppUsageDetailFragment()
        val bundle = Bundle().apply {
            putString("date", date)
            putParcelable("user", user)
        }
        fragment.arguments = bundle
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun observeAppUsageDateUiState() {
        lifecycleScope.launch {
            viewModel.appUsageDateUiState.collect { state ->
                when (state) {
                    is AppUsageDateUiState.Loading -> showLoading()
                    is AppUsageDateUiState.Success -> showDates(state.dateList)
                    is AppUsageDateUiState.Error -> showError(state.message)
                }
            }
        }
    }

    private fun showLoading() {
        binding.pbDate.visibility = View.VISIBLE
        binding.rvDate.visibility = View.GONE
        binding.tvEmptyMessage.visibility = View.GONE
    }

    private fun showDates(dates: List<AppUsageDateInfo>) {
        binding.pbDate.visibility = View.GONE
        if (dates.isEmpty()) {
            binding.tvEmptyMessage.visibility = View.VISIBLE
            binding.rvDate.visibility = View.GONE
        } else {
            binding.tvEmptyMessage.visibility = View.GONE
            binding.rvDate.visibility = View.VISIBLE
            adapter.submitList(dates)
        }
    }

    private fun showError(message: String) {
        binding.pbDate.visibility = View.GONE
        binding.rvDate.visibility = View.GONE
        binding.tvEmptyMessage.text = message
        binding.tvEmptyMessage.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}