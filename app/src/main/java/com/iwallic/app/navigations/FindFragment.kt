package com.iwallic.app.navigations

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.base.BaseFragment

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
        findWV.settings.javaScriptEnabled = true
        findWV.webViewClient = WebViewClient()

        findWV.loadUrl("file:///android_asset/fragment_find/index.html")

        findSRL.setOnRefreshListener {
            findWV.reload()
            findSRL.isRefreshing = false
            Toast.makeText(context, R.string.toast_refreshed, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        val TAG: String = FindFragment::class.java.simpleName
        fun newInstance() = FindFragment()
    }
}
