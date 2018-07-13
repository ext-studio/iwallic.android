package com.iwallic.app.base

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.iwallic.app.R
import com.iwallic.app.utils.WalletUtils
import com.iwallic.app.pages.wallet.WalletActivity
import com.iwallic.app.utils.ConfigUtils
import com.iwallic.app.utils.SharedPrefUtils
import kotlinx.coroutines.experimental.launch

class WelcomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_welcome)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        launch {
            resolveTheme()
            resolveConfig()
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

    private fun resolveTheme() {
        when (SharedPrefUtils.getSkin(this)) {
            "default" -> setTheme(R.style.ThemeDefault)
            "night" -> setTheme(R.style.ThemeNight)
            else -> setTheme(R.style.ThemeDefault)
        }
    }

    private fun resolveConfig() {
        // request for latest config, or use local config
        ConfigUtils.set(SharedPrefUtils.getNet(this), "https://api.iwallic.com/api/iwallic", "https://teapi.iwallic.com/api/iwallic")
    }
}

