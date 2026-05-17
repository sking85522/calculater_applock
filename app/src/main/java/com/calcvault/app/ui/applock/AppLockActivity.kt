package com.calcvault.app.ui.applock

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.calcvault.app.R
import com.calcvault.app.utils.PreferenceManager

class AppLockActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceManager = PreferenceManager(this)

        // Theme customization based on user preference
        if (preferenceManager.isDarkModeAppLock()) {
            setTheme(androidx.appcompat.R.style.Theme_AppCompat_NoActionBar)
        }

        setContentView(R.layout.activity_app_lock)

        val etPin = findViewById<EditText>(R.id.et_applock_pin)
        val btnUnlock = findViewById<Button>(R.id.btn_unlock)

        btnUnlock.setOnClickListener {
            val pin = etPin.text.toString()
            if (preferenceManager.verifyPin(pin)) {
                // If correct, finish this overlay activity and let the user use the app
                AppLockManager.addUnlockedApp(intent.getStringExtra("LOCKED_PACKAGE_NAME") ?: "")
                finish()
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                etPin.text.clear()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent backing out of the lock screen to the locked app.
        // Go to home screen instead.
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN)
        intent.addCategory(android.content.Intent.CATEGORY_HOME)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
        super.onBackPressed() // required by lint, though we finish above
    }
}
