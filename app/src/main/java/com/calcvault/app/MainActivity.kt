package com.calcvault.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.calcvault.app.utils.CameraHelper
import com.calcvault.app.utils.PreferenceManager
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView
    private var currentInput = ""
    private var operator = ""
    private var firstOperand = 0.0

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferenceManager = PreferenceManager(this)
        tvDisplay = findViewById(R.id.tv_display)

        setupButtons()
        checkBiometricUnlock()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        }
    }

    private fun checkBiometricUnlock() {
        if (preferenceManager.isFingerprintEnabled()) {
            val executor = ContextCompat.getMainExecutor(this)
            val biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        // Ignore error, fallback to PIN is natural because calc remains open
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Toast.makeText(applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                        openVault()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Vault")
                .setSubtitle("Use your fingerprint to access the vault")
                .setNegativeButtonText("Use Calculator PIN")
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun setupButtons() {
        val numButtons = listOf(
            R.id.btn_0 to "0", R.id.btn_1 to "1", R.id.btn_2 to "2",
            R.id.btn_3 to "3", R.id.btn_4 to "4", R.id.btn_5 to "5",
            R.id.btn_6 to "6", R.id.btn_7 to "7", R.id.btn_8 to "8",
            R.id.btn_9 to "9", R.id.btn_dot to "."
        )

        for ((id, value) in numButtons) {
            findViewById<Button>(id).setOnClickListener { appendInput(value) }
        }

        val opButtons = listOf(
            R.id.btn_plus to "+", R.id.btn_minus to "-",
            R.id.btn_mul to "*", R.id.btn_div to "/",
            R.id.btn_percent to "%"
        )

        for ((id, op) in opButtons) {
            findViewById<Button>(id).setOnClickListener { setOp(op) }
        }

        findViewById<Button>(R.id.btn_clear).setOnClickListener { clear() }
        findViewById<Button>(R.id.btn_del).setOnClickListener { deleteLast() }
        findViewById<Button>(R.id.btn_eq).setOnClickListener { calculateResult() }
    }

    private var isNewOperation = true

    private fun appendInput(value: String) {
        if (isNewOperation) {
            currentInput = ""
            isNewOperation = false
        }
        if (value == "." && currentInput.contains(".")) return

        // Don't allow multiple leading zeros
        if (currentInput == "0" && value != ".") {
            currentInput = value
        } else {
            currentInput += value
        }
        tvDisplay.text = currentInput
    }

    private fun setOp(op: String) {
        if (currentInput.isNotEmpty()) {
            if (operator.isNotEmpty() && !isNewOperation) {
                // Calculate intermediate result before setting new operator
                calculateResult(isIntermediate = true)
            }
            firstOperand = currentInput.toDoubleOrNull() ?: 0.0
            operator = op
            isNewOperation = true
        }
    }

    private fun clear() {
        currentInput = ""
        operator = ""
        firstOperand = 0.0
        isNewOperation = true
        tvDisplay.text = "0"
    }

    private fun deleteLast() {
        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
            if (currentInput.isEmpty() || currentInput == "-") {
                currentInput = "0"
                isNewOperation = true
            }
            tvDisplay.text = currentInput
        }
    }

    private var wrongPinAttempts = 0

    private fun calculateResult(isIntermediate: Boolean = false) {
        // --- SECRET VAULT CHECK (Only when user explicitly hits '=' without pending math, or if it exactly matches) ---
        // We only consider it a PIN attempt if it looks like a PIN (length 4+), no decimal, and '=' is pressed.
        if (!isIntermediate && currentInput.length >= 4 && !currentInput.contains(".")) {
            if (preferenceManager.verifyPin(currentInput)) {
                wrongPinAttempts = 0
                openVault(isFake = false)
                return
            } else if (preferenceManager.verifyFakePin(currentInput)) {
                wrongPinAttempts = 0
                openVault(isFake = true)
                return
            } else {
                // It was a pure number entry without an operator, and it failed the PIN check.
                // We track it as a wrong attempt, but we still display it.
                if (operator.isEmpty()) {
                    wrongPinAttempts++
                    if (wrongPinAttempts >= 3) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            CameraHelper(this).takeIntruderSelfie()
                        }
                        showFakeCrash()
                        wrongPinAttempts = 0 // Reset after crash
                        clear()
                        return
                    }
                }
            }
        }

        // --- NORMAL CALCULATION ---
        if (operator.isEmpty() || isNewOperation) return

        val secondOperand = currentInput.toDoubleOrNull() ?: 0.0
        var result = 0.0

        when (operator) {
            "+" -> result = firstOperand + secondOperand
            "-" -> result = firstOperand - secondOperand
            "*" -> result = firstOperand * secondOperand
            "/" -> if (secondOperand != 0.0) result = firstOperand / secondOperand else {
                Toast.makeText(this, "Cannot divide by zero", Toast.LENGTH_SHORT).show()
                clear()
                return
            }
            "%" -> result = (firstOperand / 100) * secondOperand
        }

        // Format to remove .0 if integer
        val resultStr = if (result % 1 == 0.0) result.toLong().toString() else result.toString()

        tvDisplay.text = resultStr
        currentInput = resultStr

        if (!isIntermediate) {
            operator = ""
        }
        isNewOperation = true
    }

    private fun openVault(isFake: Boolean = false) {
        currentInput = ""
        tvDisplay.text = "0"
        operator = ""
        firstOperand = 0.0

        val intent = Intent(this, com.calcvault.app.ui.vault.VaultDashboardActivity::class.java)
        intent.putExtra("IS_FAKE_VAULT", isFake)
        startActivity(intent)
    }

    private fun showFakeCrash() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Calculator")
        builder.setMessage("Unfortunately, Calculator has stopped.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            finish() // Close the app
        }
        builder.setCancelable(false)
        builder.show()
    }
}
