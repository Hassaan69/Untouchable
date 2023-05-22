package com.example.untouchable

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat


class ForegroundService : Service() {

    private var windowManager: WindowManager? = null
    private var upperLayoutView: View? = null
    private var middleLayoutView: View? = null
    private var lowerLayoutView: View? = null
    private var isRunning = false
    private var isTouchEnabled = false

    override fun onCreate() {
        super.onCreate()
        startForeground()
        addOverlayViews()
        isRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (windowManager != null) {
            windowManager!!.removeView(upperLayoutView)
            windowManager!!.removeView(middleLayoutView)
            windowManager!!.removeView(lowerLayoutView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun startForeground() {
        val channelId = "overlay_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId)
            val message = "Overlay Service Running"
            val notification: Notification = Notification.Builder(this, channelId)
                .setContentTitle("Overlay Service")
                .setContentText(message)
                .setTicker("Service Started")
                .build()
            startForeground(1002, notification)
        } else {
            val message = "Overlay Service Running"
            val notification: Notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Overlay Service")
                .setContentText(message)
                .setTicker("Service Started")
                .build()
            startForeground(1002, notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String) {
        val channel =  NotificationChannel(
            channelId,
            "My Overlay Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

        val service = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
    }

private fun addOverlayViews() {
    // Create and initialize the WindowManager
    windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

    // Inflate overlay view layouts for upper, middle, and lower sections
    upperLayoutView = LayoutInflater.from(this).inflate(R.layout.upper_layout, null)
    middleLayoutView = LayoutInflater.from(this).inflate(R.layout.middle_layout, null)
    lowerLayoutView = LayoutInflater.from(this).inflate(R.layout.lower_layout, null)

    // Set the layout parameters for the overlay views
    val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    )
    placeOverlayViews(params)
    val middleButton = middleLayoutView?.findViewById<Button>(R.id.btn_middle)

    middleButton?.setOnClickListener {
        isTouchEnabled = !isTouchEnabled
        if (isTouchEnabled) {
            middleButton.setText("Block Touch")
            // Get the dimensions of the button
            val buttonWidth = middleButton.width
            val buttonHeight = middleButton.height

            params.gravity = Gravity.CENTER
            params.height = buttonHeight
            params.width = buttonWidth
            windowManager?.updateViewLayout(middleLayoutView, params)

            params.height = 0
            params.width = 0
            windowManager?.updateViewLayout(upperLayoutView, params)
            windowManager?.updateViewLayout(lowerLayoutView, params)
        } else {
            middleButton.setText("Unblock Touch")

            // Calculate the screen height and width
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels

            // Upper overlay view covers 33% from the top
            params.gravity = Gravity.TOP or Gravity.START
            params.width = screenWidth
            params.height = (screenHeight * 0.33).toInt()
            windowManager?.updateViewLayout(upperLayoutView, params)

            // Middle overlay view covers the middle portion
            params.gravity = Gravity.CENTER
            params.x = (screenWidth - params.width) / 2
            params.y = (screenHeight - params.height) / 2
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            windowManager?.updateViewLayout(middleLayoutView, params)

            // Lower overlay view covers 33% from the bottom
            params.gravity = Gravity.BOTTOM or Gravity.START
            params.x = 0
            params.y = 0
            params.height = (screenHeight * 0.33).toInt()
            windowManager?.updateViewLayout(lowerLayoutView, params)
        }

    }
}

    private fun placeOverlayViews(params: WindowManager.LayoutParams) {
        // Calculate the screen height and width
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        // Upper overlay view covers 33% from the top
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 0
        params.height = (screenHeight * 0.33).toInt()
        windowManager?.addView(upperLayoutView, params)

        // Middle overlay view covers the middle portion
        params.gravity = Gravity.CENTER
        params.x = (screenWidth - params.width) / 2
        params.y = (screenHeight - params.height) / 2
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        windowManager?.addView(middleLayoutView, params)

        // Lower overlay view covers 33% from the bottom
        params.gravity = Gravity.BOTTOM or Gravity.START
        params.x = 0
        params.y = 0
        params.height = (screenHeight * 0.33).toInt()
        windowManager?.addView(lowerLayoutView, params)
    }
}
