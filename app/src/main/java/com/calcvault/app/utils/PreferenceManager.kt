package com.calcvault.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferenceManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun isPinSet(): Boolean {
        return sharedPreferences.contains(KEY_SECRET_PIN)
    }

    fun setPin(pin: String) {
        sharedPreferences.edit().putString(KEY_SECRET_PIN, pin).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val savedPin = sharedPreferences.getString(KEY_SECRET_PIN, null)
        return savedPin == pin
    }

    companion object {
        private const val KEY_SECRET_PIN = "key_secret_pin"
    }
}
