package com.ags.admin.ui.systemAccessScreen

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ags.admin.databinding.ActivitySystemAccessBinding
import com.ags.core.BaseActivity

class SystemAccessActivity : BaseActivity() {

    private lateinit var binding: ActivitySystemAccessBinding
    private val viewModel: SystemAccessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySystemAccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarsPadding(binding.root)


        val adapter = SystemAccessAdapter(viewModel.features) { feature ->
            Toast.makeText(this, "Clicked: ${feature.title}", Toast.LENGTH_SHORT).show()
        }
        binding.rvSystemAccess.layoutManager = LinearLayoutManager(this)
        binding.rvSystemAccess.adapter = adapter
    }
}