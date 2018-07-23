package com.iwallic.app.pages.user

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity

class UserBrowserActivity : BaseActivity() {
    private lateinit var browserWV: WebView
    private lateinit var browserSRL: SwipeRefreshLayout
    private lateinit var browserTV: TextView

    private var url: String = "file:///android_asset/commons/default.html"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_browser)
        initDOM()
        initUrl()
    }

    private fun initDOM() {
        browserWV = findViewById(R.id.user_browser_webview)
        browserSRL = findViewById(R.id.user_browser_refresh)
        browserTV = findViewById(R.id.user_browser_title)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initUrl() {
        val target = intent.getStringExtra("url")
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
    }
}
