package com.calcvault.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.calcvault.app.MainActivity

class StealthReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val host = intent.data?.host
        // If the user dials *#*#1234#*#*
        if (host == "1234") {
            val appIntent = Intent(context, MainActivity::class.java)
            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(appIntent)
        }
    }
}
