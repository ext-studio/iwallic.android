package com.iwallic.app.pages.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.base.BaseFragment
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient

class FindFragment : BaseFragment() {
//    private lateinit var findWV: WebView
//    private lateinit var findSRL: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_find, container, false)
        initDOM(view)
//        initWebView()
        return view
    }

    private fun initDOM(view: View) {
//        findWV = view.findViewById(R.id.fragment_find_webview)
//        findSRL = view.findViewById(R.id.fragment_find_refresh)
//        findSRL.setColorSchemeResources(R.color.colorPrimaryDefault)
    }
}
