package com.calcvault.app.ui.applock

object AppLockManager {
    val lockedApps = mutableSetOf<String>()

    // Temporarily unlocked apps during the current session
    private val temporarilyUnlocked = mutableSetOf<String>()

    fun addUnlockedApp(packageName: String) {
        temporarilyUnlocked.add(packageName)
    }

    fun isAppUnlocked(packageName: String): Boolean {
        return temporarilyUnlocked.contains(packageName)
    }

    fun lockAll() {
        temporarilyUnlocked.clear()
    }
}
