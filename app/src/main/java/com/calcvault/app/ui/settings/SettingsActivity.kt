package com.calcvault.app.ui.settings

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.calcvault.app.R
import com.calcvault.app.utils.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferenceManager = PreferenceManager(this)

        val btnChangePin = findViewById<Button>(R.id.btn_change_pin)
        val switchFingerprint = findViewById<Switch>(R.id.switch_fingerprint)

        switchFingerprint.isChecked = preferenceManager.isFingerprintEnabled()
        switchFingerprint.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setFingerprintEnabled(isChecked)
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Fingerprint $status", Toast.LENGTH_SHORT).show()
        }

        btnChangePin.setOnClickListener {
            showChangePinDialog()
        }
    }

    private fun showChangePinDialog() {
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
                Toast.makeText(this, "Old PIN is incorrect", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (newPin.isEmpty() || newPin != confirmPin) {
                Toast.makeText(this, "New PINs do not match or are empty", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            preferenceManager.setPin(newPin)
            Toast.makeText(this, "PIN changed successfully!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }
}
