package com.iwallic.app.pages.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity

class UserSupportActivity : BaseActivity() {
    private lateinit var backLL: LinearLayout
    private lateinit var walletGuideLL: LinearLayout
    private lateinit var transactionGuideLL: LinearLayout
    private lateinit var browser: LinearLayout
    private lateinit var community: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_support)

        initDOM()
        initClick()
    }

    private fun initDOM() {
        backLL = findViewById(R.id.user_support_back)
        walletGuideLL = findViewById(R.id.activity_user_support_wallet_guide)
        transactionGuideLL = findViewById(R.id.activity_user_support_transaction_guide)
        browser = findViewById(R.id.activity_user_support_blockchain_browser)
        community = findViewById(R.id.activity_user_support_community)
    }

    private fun initClick() {
        walletGuideLL.setOnClickListener {
            Toast.makeText(this, R.string.error_incoming, Toast.LENGTH_SHORT).show()
        }

        transactionGuideLL.setOnClickListener {
            Toast.makeText(this, R.string.error_incoming, Toast.LENGTH_SHORT).show()
        }

        browser.setOnClickListener {
            val intent = Intent(this, UserBrowserActivity::class.java)
            intent.putExtra("url", "https://blolys.com")
            startActivity(intent)
        }

        community.setOnClickListener {
            val intent = Intent(this, UserBrowserActivity::class.java)
            intent.putExtra("url", "https://bbs.iwallic.com")
            startActivity(intent)
        }

        backLL.setOnClickListener {
            finish()
        }
    }
}
