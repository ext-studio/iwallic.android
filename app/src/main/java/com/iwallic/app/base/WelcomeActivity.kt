package com.iwallic.app.base

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.iwallic.app.R
import com.iwallic.app.utils.WalletUtils
import com.iwallic.app.pages.wallet.WalletActivity
import kotlinx.coroutines.experimental.launch

class WelcomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_welcome)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        launch {
            if (WalletUtils.wallet(baseContext) == null || WalletUtils.account(baseContext) == null) {
                startActivity(Intent(baseContext, WalletActivity::class.java))
                finish()
                return@launch
            }
            startActivity(Intent(baseContext, MainActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        return
    }
}

