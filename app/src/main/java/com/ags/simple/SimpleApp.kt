package com.ags.simple

import android.app.Application
import com.google.android.material.color.DynamicColors

class SimpleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply Material You dynamic colors to all activities, including splash
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}