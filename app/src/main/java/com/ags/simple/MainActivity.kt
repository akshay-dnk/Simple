package com.ags.simple

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.ags.admin.AdminDashboardActivity
import com.ags.core.BaseActivity
import com.ags.simple.databinding.ActivityMainBinding
import com.ags.simple.ui.authScreen.AuthActivity
import com.ags.simple.ui.authScreen.AuthState
import com.ags.simple.ui.authScreen.AuthViewModel
import com.ags.simple.ui.authScreen.RoleState
import com.ags.user.UserDashboardActivity
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AuthViewModel by viewModels()

    private var isReady = false
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {

        // Install and configure splash
        val splashScreen = installSplashScreen()
        setupSplashScreen(splashScreen)

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarsPadding(binding.root)

        // Observe auth state
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {

                    is AuthState.FirstLaunch -> navigateToAuth()

                    is AuthState.SignedIn -> {
                        when (state.role) {
                            RoleState.Admin -> {
                                startActivity(Intent(this@MainActivity, AdminDashboardActivity::class.java))
                            }

                            RoleState.User -> {
                                startActivity(Intent(this@MainActivity, UserDashboardActivity::class.java))
                            }
                        }
                        finish()
                    }

                    is AuthState.SignedOut-> navigateToAuth()
                    is AuthState.Error -> navigateToAuth()
                    is AuthState.Idle,
                    is AuthState.Loading -> Unit
                }
                // Mark ready once we get a state (other than Loading/Idle)
                if (state is AuthState.SignedIn ||
                    state is AuthState.SignedOut ||
                    state is AuthState.Error ||
                    state is AuthState.FirstLaunch
                ) {
                    isReady = true
                }
            }
        }

    }

    private fun navigateToAuth() {
        if (hasNavigated) return
        hasNavigated = true
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }

    private fun setupSplashScreen(splashScreen: SplashScreen) {
        // Keep splash until 'isReady' becomes true
        splashScreen.setKeepOnScreenCondition { !isReady }

        // Exit animation
        splashScreen.setOnExitAnimationListener { splashView ->
            splashView.iconView.animate()
                .setDuration(300L)
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .translationY(-50f)
                .withEndAction { splashView.remove() }
        }
    }
}