package com.ags.simple

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {

        // Install the splash screen
        val splashScreen = installSplashScreen()

        // splash handling
        setupSplashScreen(splashScreen)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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

        // Simulate some startup work (like loading DB / API)
        lifecycleScope.launch {
            delay(2000) // Simulated loading
            isReady = true
        }
    }
}