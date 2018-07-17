package com.iwallic.app.pages.wallet

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity

class WalletActivity : BaseActivity() {
    private lateinit var createB: Button
    private lateinit var importB: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_wallet)
        initDOM()
        initListener()
    }

    private fun initDOM() {
        createB = findViewById(R.id.wallet_create_btn)
        importB = findViewById(R.id.wallet_import_btn)
    }

    private fun initListener() {
        createB.setOnClickListener {
            startActivity(Intent(this, WalletCreateActivity::class.java))
        }

        importB.setOnClickListener {
            startActivity(Intent(this, WalletImportActivity::class.java))
        }
    }
}
