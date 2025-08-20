package com.ags.admin.ui.userDetailScreen

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.ags.admin.databinding.ActivityUserDetailBinding
import com.ags.admin.model.User
import com.ags.admin.ui.systemAccessScreen.SystemAccessActivity
import com.ags.core.BaseActivity

class UserDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityUserDetailBinding
    private lateinit var user: User   // hold full user object

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        user = intent.getParcelableExtra("user", User::class.java) ?: return finish()

        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarsPadding(binding.root)

        binding.btnSystemAccess.setOnClickListener {
            val intent = Intent(this, SystemAccessActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }
    }
}