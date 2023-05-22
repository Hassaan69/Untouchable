package com.example.untouchable

import android.app.Application
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

class Untouchable : Application() , DefaultLifecycleObserver{
    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Settings.canDrawOverlays(this))
                startForegroundService(Intent(this@Untouchable,ForegroundService::class.java))
        } else {
            if (Settings.canDrawOverlays(this))
                 startService(Intent(this@Untouchable, ForegroundService::class.java))
        }
        super.onStop(owner)
    }
}