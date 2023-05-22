package com.example.untouchable

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var systemAlertWindowPermissionLauncher: ActivityResultLauncher<Intent>

    private val PERMISSION_POST_NOTIFICATIONS = Manifest.permission.POST_NOTIFICATIONS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            handlePermissionResults(permissions)
        }

        systemAlertWindowPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            checkPermissions()
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        val overlayPermissionGranted = Settings.canDrawOverlays(this)
        val notificationPermissionGranted = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(PERMISSION_POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Assume notification permission is granted for versions below Android 12
        }

        if (!overlayPermissionGranted || !notificationPermissionGranted) {
            // Overlay permission or notification permission not granted, show a dialog to the user
            showPermissionDialog()
        }
    }

    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permissions Required")
            .setMessage("Please grant the required permissions to use this app.")
            .setPositiveButton("Grant") { dialog, _ ->
                if (!Settings.canDrawOverlays(this)) {
                    requestOverlayPermission()
                } else {
                    requestNotificationPermission()
                }
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        systemAlertWindowPermissionLauncher.launch(intent)
    }

    private fun requestNotificationPermission() {
        val ungrantedPermissions = arrayOf(PERMISSION_POST_NOTIFICATIONS)
        notificationPermissionLauncher.launch(ungrantedPermissions)
    }

    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        val notificationPermissionGranted = permissions[PERMISSION_POST_NOTIFICATIONS] ?: false

        if (!notificationPermissionGranted) {
            // Notification permission not granted, inform the user
            Toast.makeText(
                this,
                "Notification permission not granted. Service Notification won't be visible.",
                Toast.LENGTH_SHORT
            ).show()
        }

        if (!Settings.canDrawOverlays(this)) {
            // Overlay permission not granted, inform the user
            Toast.makeText(
                this,
                "Overlay permission not granted. Service Notification won't be visible.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}
