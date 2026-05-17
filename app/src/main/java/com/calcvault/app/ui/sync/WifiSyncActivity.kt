package com.calcvault.app.ui.sync

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.calcvault.app.R
import com.calcvault.app.ui.base.BaseVaultActivity

class WifiSyncActivity : BaseVaultActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_sync)

        val tvIp = findViewById<TextView>(R.id.tv_ip_address)

        // In a real implementation, we would fetch the actual device IP via WifiManager
        // and start a NanoHTTPD server here.
        tvIp.text = "http://192.168.1.55:8080 (Demo)"

        findViewById<Button>(R.id.btn_stop_sync).setOnClickListener {
            finish()
        }
    }
}
