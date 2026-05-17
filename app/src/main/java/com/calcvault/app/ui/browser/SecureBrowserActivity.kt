package com.calcvault.app.ui.browser

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import com.calcvault.app.R
import com.calcvault.app.ui.base.BaseVaultActivity

class SecureBrowserActivity : BaseVaultActivity() {

    private lateinit var webView: WebView
    private lateinit var etUrl: EditText

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secure_browser)

        webView = findViewById(R.id.webview)
        etUrl = findViewById(R.id.et_url)
        val btnGo = findViewById<Button>(R.id.btn_go)

        // Configure WebView for Incognito/Secure mode
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        webSettings.savePassword = false
        webSettings.saveFormData = false

        // Disable cookies
        CookieManager.getInstance().setAcceptCookie(false)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                etUrl.setText(url)
            }
        }

        btnGo.setOnClickListener { loadUrl() }

        etUrl.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                loadUrl()
                true
            } else {
                false
            }
        }

        // Load a default secure search engine
        webView.loadUrl("https://duckduckgo.com")
    }

    private fun loadUrl() {
        var url = etUrl.text.toString().trim()
        if (url.isNotEmpty()) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }
            webView.loadUrl(url)
        }
    }

    override fun onDestroy() {
        // Clear all data when exiting to ensure privacy
        webView.clearCache(true)
        webView.clearHistory()
        webView.clearFormData()
        CookieManager.getInstance().removeAllCookies(null)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
