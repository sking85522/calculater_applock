package com.calcvault.app.ui.applock

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import com.calcvault.app.R
import com.calcvault.app.services.AppLockService
import com.calcvault.app.ui.base.BaseVaultActivity

class ManageAppLockActivity : BaseVaultActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // For simplicity in this foundation we use a basic placeholder UI
        // In a full implementation, a RecyclerView with installed apps (PackageManager.getInstalledPackages) would go here
        setContentView(R.layout.activity_manage_app_lock)

        findViewById<Button>(R.id.btn_grant_usage).setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        findViewById<Button>(R.id.btn_lock_test_app).setOnClickListener {
            // Lock YouTube as a test case
            AppLockManager.lockedApps.add("com.google.android.youtube")
            Toast.makeText(this, "YouTube Locked for testing!", Toast.LENGTH_SHORT).show()

            // Start the monitoring service
            val serviceIntent = Intent(this, AppLockService::class.java)
            startService(serviceIntent)
        }
    }
}
