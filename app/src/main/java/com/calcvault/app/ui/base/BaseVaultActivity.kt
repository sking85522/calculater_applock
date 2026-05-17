package com.calcvault.app.ui.base

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.calcvault.app.utils.PanicSensorManager

abstract class BaseVaultActivity : AppCompatActivity() {

    private lateinit var panicSensorManager: PanicSensorManager

    override fun onResume() {
        super.onResume()
        panicSensorManager = PanicSensorManager(this) {
            triggerPanicExit()
        }
        panicSensorManager.start()
    }

    override fun onPause() {
        super.onPause()
        if (::panicSensorManager.isInitialized) {
            panicSensorManager.stop()
        }
    }

    private fun triggerPanicExit() {
        // Go back to the device home screen
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finishAffinity() // Closes all activities in the app
    }
}
