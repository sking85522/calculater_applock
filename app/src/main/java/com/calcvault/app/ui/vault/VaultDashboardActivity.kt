package com.calcvault.app.ui.vault

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.calcvault.app.R
import com.calcvault.app.ui.settings.SettingsActivity

class VaultDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vault_dashboard)

        findViewById<Button>(R.id.btn_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.btn_photos).setOnClickListener {
            startActivity(Intent(this, com.calcvault.app.ui.photos.PhotoVaultActivity::class.java))
        }

        findViewById<Button>(R.id.btn_videos).setOnClickListener {
            startActivity(Intent(this, com.calcvault.app.ui.videos.VideoVaultActivity::class.java))
        }
    }
}
