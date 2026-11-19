package com.example

import android.app.Application
import com.example.ads.UnityAdsManager

class BongCalculatorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        UnityAdsManager.initialize(this)
    }
}
