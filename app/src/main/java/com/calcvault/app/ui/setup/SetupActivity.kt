package com.calcvault.app.ui.setup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.calcvault.app.MainActivity
import com.calcvault.app.R
import com.calcvault.app.utils.PreferenceManager

class SetupActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager
    private var firstPin: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(this)

        // If PIN is already set, jump straight to the Calculator (MainActivity)
        if (preferenceManager.isPinSet()) {
            startMainActivity()
            return
        }

        setContentView(R.layout.activity_setup)

        val etPin = findViewById<EditText>(R.id.et_pin)
        val btnNext = findViewById<Button>(R.id.btn_next)

        btnNext.setOnClickListener {
            val inputPin = etPin.text.toString()
            if (inputPin.isEmpty()) {
                Toast.makeText(this, "Please enter a PIN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (firstPin == null) {
                // First step: User entered their PIN
                firstPin = inputPin
                etPin.text.clear()
                etPin.hint = "Confirm your PIN"
                btnNext.text = "Confirm"
            } else {
                // Second step: User confirms their PIN
                if (firstPin == inputPin) {
                    preferenceManager.setPin(inputPin)
                    Toast.makeText(this, "PIN Setup Successful!", Toast.LENGTH_SHORT).show()
                    startMainActivity()
                } else {
                    Toast.makeText(this, "PINs do not match. Try again.", Toast.LENGTH_SHORT).show()
                    firstPin = null
                    etPin.text.clear()
                    etPin.hint = "Enter your secret PIN"
                    btnNext.text = "Next"
                }
            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
