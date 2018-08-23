package com.iwallic.app.pages.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity

class UserSupportActivity : BaseActivity() {
    private lateinit var backTV: TextView
    private lateinit var walletGuideFL: FrameLayout
    private lateinit var transactionGuideFL: FrameLayout
    private lateinit var browser: FrameLayout
    // private lateinit var community: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_support)

        initDOM()
        initClick()
    }

    private fun initDOM() {
        backTV = findViewById(R.id.user_support_back)
        walletGuideFL = findViewById(R.id.activity_user_support_wallet_guide)
        transactionGuideFL = findViewById(R.id.activity_user_support_transaction_guide)
        browser = findViewById(R.id.activity_user_support_blockchain_browser)
        // community = findViewById(R.id.activity_user_support_community)
    }

    private fun initClick() {
        walletGuideFL.setOnClickListener {
            val intent = Intent(this, UserBrowserActivity::class.java)
            intent.putExtra("url", "https://iwallic.com/assets/questions")
            startActivity(intent)
        }

        transactionGuideFL.setOnClickListener {
            Toast.makeText(this, R.string.error_incoming, Toast.LENGTH_SHORT).show()
        }

        browser.setOnClickListener {
            val intent = Intent(this, UserBrowserActivity::class.java)
            intent.putExtra("url", "https://blolys.com")
            startActivity(intent)
        }

//        community.setOnClickListener {
//            val intent = Intent(this, UserBrowserActivity::class.java)
//            intent.putExtra("url", "https://bbs.iwallic.com")
//            startActivity(intent)
//        }

        backTV.setOnClickListener {
            finish()
        }
    }
}
