package com.calcvault.app.services

import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.calcvault.app.ui.applock.AppLockActivity
import com.calcvault.app.ui.applock.AppLockManager
import kotlinx.coroutines.*

class AppLockService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var isMonitoring = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isMonitoring) {
            isMonitoring = true
            monitorForegroundApp()
        }
        return START_STICKY
    }

    private fun monitorForegroundApp() {
        serviceScope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

            while (isMonitoring) {
                val time = System.currentTimeMillis()
                val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)

                if (stats != null) {
                    var topPackageName = ""
                    var lastTimeUsed = 0L
                    for (usageStats in stats) {
                        if (usageStats.lastTimeUsed > lastTimeUsed) {
                            topPackageName = usageStats.packageName
                            lastTimeUsed = usageStats.lastTimeUsed
                        }
                    }

                    if (topPackageName.isNotEmpty()) {
                        checkIfAppNeedsLock(topPackageName)
                    }
                }
                delay(500) // Poll every 500ms
            }
        }
    }

    private fun checkIfAppNeedsLock(packageName: String) {
        // Skip locking ourselves
        if (packageName == this.packageName) return

        if (AppLockManager.lockedApps.contains(packageName) && !AppLockManager.isAppUnlocked(packageName)) {
            val lockIntent = Intent(this, AppLockActivity::class.java)
            lockIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            lockIntent.putExtra("LOCKED_PACKAGE_NAME", packageName)
            startActivity(lockIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isMonitoring = false
        serviceScope.cancel()
    }
}
