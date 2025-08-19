package com.ags.simple.ui.authScreen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ags.admin.ui.dashboardScreen.AdminDashboardActivity
import com.ags.core.BaseActivity
import com.ags.simple.databinding.ActivityAuthBinding
import com.ags.user.UserDashboardActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthActivity : BaseActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarsPadding(binding.root)

        setupListeners()
        observeAuthState()
        startTaglineRotation()
    }

    private fun setupListeners() {
        binding.btnGoogleSignIn.setOnClickListener {
            viewModel.signIn(this)
        }
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {

                    is AuthState.FirstLaunch, is AuthState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnGoogleSignIn.isEnabled = true
                    }

                    is AuthState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnGoogleSignIn.isEnabled = false
                    }

                    is AuthState.SignedIn -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnGoogleSignIn.isEnabled = true
                        when (state.role) {
                            RoleState.Admin -> {
                                startActivity(Intent(this@AuthActivity, AdminDashboardActivity::class.java))
                            }

                            RoleState.User -> {
                                startActivity(Intent(this@AuthActivity, UserDashboardActivity::class.java))
                            }
                        }
                        finishAffinity()
                    }

                    is AuthState.SignedOut -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnGoogleSignIn.isEnabled = true
                        Toast.makeText(this@AuthActivity, "Signed out", Toast.LENGTH_SHORT).show()
                    }

                    is AuthState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnGoogleSignIn.isEnabled = true
                        Toast.makeText(this@AuthActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    private fun startTaglineRotation() {

        val taglines = listOf(
            "Smart. Simple. Seamless.",
            "One tap, endless possibilities.",
            "Connect. Explore. Grow."
        )
        var taglineIndex = 0

        // Set first tagline immediately
        binding.tvTagline.text = taglines[taglineIndex]

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    val nextText = taglines[taglineIndex % taglines.size]

                    binding.tvTagline.animate()
                        .translationY(30f)
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction {
                            binding.tvTagline.text = nextText
                            binding.tvTagline.translationY = -30f
                            binding.tvTagline.animate()
                                .translationY(0f)
                                .alpha(1f)
                                .setDuration(500)
                                .start()
                        }
                        .start()

                    taglineIndex++
                    delay(3000) // wait 3 seconds before switching
                }
            }
        }

    }
}