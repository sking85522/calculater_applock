package com.calcvault.app.ui.sync

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.calcvault.app.R
import com.calcvault.app.ui.base.BaseVaultActivity

class CloudBackupActivity : BaseVaultActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_backup)

        findViewById<Button>(R.id.btn_google_drive).setOnClickListener {
            Toast.makeText(this, "Google Sign-In API needed", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_backup_now).setOnClickListener {
            Toast.makeText(this, "Backing up securely...", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_restore_now).setOnClickListener {
            Toast.makeText(this, "Restoring from cloud...", Toast.LENGTH_SHORT).show()
        }
    }
}
