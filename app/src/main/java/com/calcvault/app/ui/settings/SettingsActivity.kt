package com.calcvault.app.ui.settings

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import com.calcvault.app.R
import com.calcvault.app.ui.base.BaseVaultActivity
import com.calcvault.app.utils.PreferenceManager

class SettingsActivity : BaseVaultActivity() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferenceManager = PreferenceManager(this)

        val btnChangePin = findViewById<Button>(R.id.btn_change_pin)
        val btnSetFakePin = findViewById<Button>(R.id.btn_set_fake_pin)
        val switchFingerprint = findViewById<Switch>(R.id.switch_fingerprint)

        switchFingerprint.isChecked = preferenceManager.isFingerprintEnabled()
        switchFingerprint.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setFingerprintEnabled(isChecked)
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Fingerprint $status", Toast.LENGTH_SHORT).show()
        }

        val switchDarkMode = findViewById<Switch>(R.id.switch_dark_mode)
        switchDarkMode.isChecked = preferenceManager.isDarkModeAppLock()
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setDarkModeAppLock(isChecked)
        }

        btnChangePin.setOnClickListener {
            showChangePinDialog(false)
        }

        btnSetFakePin.setOnClickListener {
            showChangePinDialog(true)
        }

        findViewById<Button>(R.id.btn_disguise_icon).setOnClickListener {
            changeAppIcon()
        }

        findViewById<Button>(R.id.btn_stealth_mode).setOnClickListener {
            enableStealthMode()
        }
    }

    private fun changeAppIcon() {
        val pm = packageManager

        // Enable Weather alias
        pm.setComponentEnabledSetting(
            ComponentName(this, "com.calcvault.app.WeatherAlias"),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        // Disable original launcher
        pm.setComponentEnabledSetting(
            ComponentName(this, "com.calcvault.app.ui.setup.SetupActivity"),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        Toast.makeText(this, "App disguised as Weather! It may take a few seconds to update.", Toast.LENGTH_LONG).show()
    }

    private fun enableStealthMode() {
        val pm = packageManager
        // Disable both original and alias, completely hiding the app
        pm.setComponentEnabledSetting(
            ComponentName(this, "com.calcvault.app.ui.setup.SetupActivity"),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            ComponentName(this, "com.calcvault.app.WeatherAlias"),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        Toast.makeText(this, "Stealth Mode Enabled! Dial *#*#1234#*#* to open.", Toast.LENGTH_LONG).show()
    }

    private fun showChangePinDialog(isFake: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change PIN")

        val view = layoutInflater.inflate(R.layout.dialog_change_pin, null)
        val etOldPin = view.findViewById<EditText>(R.id.et_old_pin)
        val etNewPin = view.findViewById<EditText>(R.id.et_new_pin)
        val etConfirmPin = view.findViewById<EditText>(R.id.et_confirm_pin)

        builder.setView(view)

        builder.setPositiveButton("Save") { dialog, _ ->
            val oldPin = etOldPin.text.toString()
            val newPin = etNewPin.text.toString()
            val confirmPin = etConfirmPin.text.toString()

            if (!preferenceManager.verifyPin(oldPin)) {
                Toast.makeText(this, "Master/Old PIN is incorrect", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (newPin.isEmpty() || newPin != confirmPin) {
                Toast.makeText(this, "New PINs do not match or are empty", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (isFake) {
                preferenceManager.setFakePin(newPin)
                Toast.makeText(this, "Fake PIN set successfully!", Toast.LENGTH_SHORT).show()
            } else {
                preferenceManager.setPin(newPin)
                Toast.makeText(this, "PIN changed successfully!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }
}
