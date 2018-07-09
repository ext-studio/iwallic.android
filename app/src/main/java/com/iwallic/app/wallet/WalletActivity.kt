package com.iwallic.app.wallet

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity

class WalletActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_wallet)

        findViewById<Button>(R.id.wallet_create_btn).setOnClickListener {
            startActivity(Intent(this, WalletCreateActivity::class.java))
        }

        findViewById<Button>(R.id.wallet_import_btn).setOnClickListener {
            startActivity(Intent(this, WalletImportActivity::class.java))
        }
    }
}
