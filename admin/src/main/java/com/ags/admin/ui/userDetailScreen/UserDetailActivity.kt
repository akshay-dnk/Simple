package com.ags.admin.ui.userDetailScreen

import android.content.Intent
import android.os.Bundle
import com.ags.admin.databinding.ActivityUserDetailBinding
import com.ags.admin.ui.systemAccessScreen.SystemAccessActivity
import com.ags.core.BaseActivity

class UserDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityUserDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarsPadding(binding.root)

        binding.btnSystemAccess.setOnClickListener {
            val intent = Intent(this, SystemAccessActivity::class.java)
            startActivity(intent)
        }
    }
}