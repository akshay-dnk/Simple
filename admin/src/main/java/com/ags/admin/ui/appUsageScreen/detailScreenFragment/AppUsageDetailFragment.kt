package com.ags.admin.ui.appUsageScreen.detailScreenFragment

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
import com.ags.admin.databinding.FragmentAppUsageDetailBinding
import com.ags.admin.model.User
import com.ags.admin.ui.appUsageScreen.AppUsageViewModel
import com.ags.core.model.AppUsageInfo
import kotlinx.coroutines.launch


class AppUsageDetailFragment : Fragment() {

    private var _binding: FragmentAppUsageDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppUsageViewModel by viewModels()
    private lateinit var adapter: AppUsageDetailAdapter
    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAppUsageDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user = requireActivity().intent.getParcelableExtra("user", User::class.java)!!

        setupRecyclerView()
        observeAppUsageDetailUiState()

        val date = arguments?.getString("date")
        if (date != null) {
            viewModel.loadAppUsageDetail(user.email, date)
        }
    }

    private fun setupRecyclerView() {
        adapter = AppUsageDetailAdapter()
        binding.rvAppUsageDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAppUsageDetail.adapter = adapter
    }

    private fun observeAppUsageDetailUiState() {
        lifecycleScope.launch {
            viewModel.appUsageDetailUiState.collect { state ->
                when (state) {
                    is AppUsageDetailUiState.Loading -> showLoading()
                    is AppUsageDetailUiState.Success -> showAppUsageDetail(state.appUsageList)
                    is AppUsageDetailUiState.Error -> showError(state.message)
                }
            }
        }
    }

    private fun showLoading() {
        binding.pbAppUsageDetail.visibility = View.VISIBLE
        binding.rvAppUsageDetail.visibility = View.GONE
        binding.tvEmptyMessage.visibility = View.GONE
    }

    private fun showAppUsageDetail(appUsageList: List<AppUsageInfo>) {
        binding.pbAppUsageDetail.visibility = View.GONE
        if (appUsageList.isEmpty()) {
            binding.tvEmptyMessage.visibility = View.VISIBLE
            binding.rvAppUsageDetail.visibility = View.GONE
        } else {
            binding.tvEmptyMessage.visibility = View.GONE
            binding.rvAppUsageDetail.visibility = View.VISIBLE
            adapter.submitList(appUsageList)
        }
    }

    private fun showError(message: String) {
        binding.pbAppUsageDetail.visibility = View.GONE
        binding.rvAppUsageDetail.visibility = View.GONE
        binding.tvEmptyMessage.text = message
        binding.tvEmptyMessage.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}