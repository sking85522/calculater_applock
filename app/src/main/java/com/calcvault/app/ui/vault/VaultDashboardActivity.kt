package com.calcvault.app.ui.vault

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.calcvault.app.R
import com.calcvault.app.ui.base.BaseVaultActivity
import com.calcvault.app.ui.settings.SettingsActivity

class VaultDashboardActivity : BaseVaultActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vault_dashboard)

        findViewById<Button>(R.id.btn_settings).setOnClickListener {
            startVaultActivity(SettingsActivity::class.java)
        }

        findViewById<Button>(R.id.btn_photos).setOnClickListener {
            startVaultActivity(com.calcvault.app.ui.photos.PhotoVaultActivity::class.java)
        }

        findViewById<Button>(R.id.btn_browser).setOnClickListener {
            startVaultActivity(com.calcvault.app.ui.browser.SecureBrowserActivity::class.java)
        }

        findViewById<Button>(R.id.btn_notes).setOnClickListener {
            startVaultActivity(com.calcvault.app.ui.notes.SecureNotesActivity::class.java)
        }

        findViewById<Button>(R.id.btn_documents).setOnClickListener {
            startVaultActivity(com.calcvault.app.ui.documents.DocumentVaultActivity::class.java)
        }

        findViewById<Button>(R.id.btn_applock).setOnClickListener {
            startVaultActivity(com.calcvault.app.ui.applock.ManageAppLockActivity::class.java)
        }

        findViewById<Button>(R.id.btn_trash).setOnClickListener {
            startVaultActivity(com.calcvault.app.ui.trash.TrashActivity::class.java)
        }

        findViewById<Button>(R.id.btn_sync).setOnClickListener {
            startVaultActivity(com.calcvault.app.ui.sync.WifiSyncActivity::class.java)
        }

        findViewById<Button>(R.id.btn_backup).setOnClickListener {
            startVaultActivity(com.calcvault.app.ui.sync.CloudBackupActivity::class.java)
        }
    }

    private fun startVaultActivity(activityClass: Class<*>) {
        val isFake = intent.getBooleanExtra("IS_FAKE_VAULT", false)
        val newIntent = Intent(this, activityClass)
        newIntent.putExtra("IS_FAKE_VAULT", isFake)
        startActivity(newIntent)
    }
}
