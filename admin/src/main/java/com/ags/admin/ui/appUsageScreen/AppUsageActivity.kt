package com.ags.admin.ui.appUsageScreen

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.ags.admin.R
import com.ags.admin.databinding.ActivityAppUsageBinding
import com.ags.admin.model.User
import com.ags.admin.ui.appUsageScreen.dateScreenFragment.AppUsageDateFragment
import com.ags.core.BaseActivity

class AppUsageActivity : BaseActivity() {

    private lateinit var binding: ActivityAppUsageBinding
    private lateinit var user: User

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        user = intent.getParcelableExtra("user", User::class.java) ?: return finish()

        binding = ActivityAppUsageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarsPadding(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AppUsageDateFragment())
                .commit()
        }
    }
}