package com.iwallic.app.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.gson.Gson
import com.iwallic.app.BuildConfig
import com.iwallic.app.R
import com.iwallic.app.models.OldConfig
import com.iwallic.app.pages.wallet.WalletActivity
import com.iwallic.app.services.UpdateIntentService
import com.iwallic.app.utils.*
import io.reactivex.Observable
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.*

class WelcomeActivity : BaseActivity() {
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_welcome)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        resolveConfig()
        UpdateIntentService.versionCheck(this)
        resolveWallet()
    }

    override fun onBackPressed() {
        return
    }

    private fun resolveConfig() {
        // request for latest config, or use local config
        ConfigUtils.set(SharedPrefUtils.getNet(this), "http://101.132.97.9:8001/api/iwallic", "http://101.132.97.9:8002/api/iwallic")
    }

    private fun resolveWallet() {
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
}

