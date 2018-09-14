package com.iwallic.app.pages.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.base.BaseFragment
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient

class FindFragment : BaseFragment() {
    private lateinit var findWV: WebView
    private lateinit var findSRL: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_find, container, false)
        initDOM(view)
        initWebView()
        return view
    }

    private fun initDOM(view: View) {
        findWV = view.findViewById(R.id.fragment_find_webview)
        findSRL = view.findViewById(R.id.fragment_find_refresh)
        findSRL.setColorSchemeResources(R.color.colorPrimaryDefault)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        findWV.webViewClient = WebViewClient()
        val webSetting = findWV.settings

        webSetting.allowFileAccess = true
        webSetting.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        webSetting.setSupportZoom(true)
//        webSetting.builtInZoomControls = true
        webSetting.useWideViewPort = true
        webSetting.setSupportMultipleWindows(false)
        // webSetting.setLoadWithOverviewMode(true)
        webSetting.setAppCacheEnabled(true)
        // webSetting.setDatabaseEnabled(true)
        webSetting.domStorageEnabled = true
        webSetting.javaScriptEnabled = true
        webSetting.setGeolocationEnabled(true)
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE)
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.pluginState = WebSettings.PluginState.ON_DEMAND

        findWV.loadUrl("file:///android_asset/fragment_find/index.html")

        findSRL.setOnRefreshListener {
            findWV.reload()
            findSRL.isRefreshing = false
            Toast.makeText(context, R.string.toast_refreshed, Toast.LENGTH_SHORT).show()
        }
    }
}
