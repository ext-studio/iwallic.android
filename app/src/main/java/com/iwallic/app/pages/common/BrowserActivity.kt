package com.iwallic.app.pages.common

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity

class BrowserActivity : BaseActivity() {
    private lateinit var browserWV: WebView
    private lateinit var browserSRL: SwipeRefreshLayout
    private lateinit var browserTV: TextView
    private lateinit var backTV: TextView

    private var url: String = "file:///android_asset/commons/default.html"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)
        initDOM()
        initUrl()
    }

    private fun initDOM() {
        browserWV = findViewById(R.id.browser_webview)
        browserSRL = findViewById(R.id.browser_refresh)
        browserTV = findViewById(R.id.browser_title)
        backTV = findViewById(R.id.browser_back)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initUrl() {
        val target = intent.getStringExtra("url") ?: ""
        if (target.isNotEmpty()) {
            url = target
        }
        browserWV.settings.javaScriptEnabled = true
        browserWV.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                browserTV.text = view?.title ?: ""
            }
        }
        browserWV.loadUrl(url)

        browserSRL.setOnRefreshListener {
            browserWV.reload()
            browserSRL.isRefreshing = false
            Toast.makeText(this, R.string.toast_refreshed, Toast.LENGTH_SHORT).show()
        }
        backTV.setOnClickListener {
            finish()
        }
    }
}
