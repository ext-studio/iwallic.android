package com.iwallic.app.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import com.google.gson.Gson
import com.iwallic.app.BuildConfig
import com.iwallic.app.R
import com.iwallic.app.models.OldConfigRes
import com.iwallic.app.models.VersionAndroidRes
import com.iwallic.app.models.VersionRes
import com.iwallic.app.pages.wallet.WalletActivity
import com.iwallic.app.utils.*
import kotlinx.coroutines.experimental.launch

class WelcomeActivity : BaseActivity() {
    private lateinit var enterB: Button
    private val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_welcome)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        enterB = findViewById(R.id.welcome_enter)
        enterB.setOnClickListener {
            resolveWallet()
        }
        resolveConfig()
        resolveVersion()
    }

    override fun onBackPressed() {
        return
    }

    private fun resolveVersion () {
        HttpClient.getPy("/client/index/app_version/detail", {
            val config = gson.fromJson(it, VersionRes::class.java)
            Log.i("【WelcomeActivity】", "version【$config】")
            if (config.code > BuildConfig.VERSION_CODE) {
                resolveNewVersion(config)
                return@getPy
            }
            resolveWallet()
        }, {
            if (!DialogUtils.error(this, it)) {
                Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
            }
            resolveWallet()
        })
    }

    private fun resolveNewVersion(config: VersionRes) {
        Log.i("【WelcomeActivity】", "new version")
        DialogUtils.confirm(
            this,
            R.string.dialog_title_primary,
            R.string.dialog_version_new_body,
            R.string.dialog_version_ok,
            R.string.dialog_no
        ).subscribe {
            if (it) {
                enterB.visibility = View.VISIBLE
                val uri = Uri.parse(config.url)
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            } else {
                resolveWallet()
            }
        }
    }

    private fun resolveConfig() {
        // 101.132.97.9:45005 192.168.1.106:5000
        // request for latest config, or use local config
        ConfigUtils.set(
                SharedPrefUtils.getNet(this),
                "http://101.132.97.9:45005",
                "http://101.132.97.9:8001/api/iwallic",
                "http://101.132.97.9:8002/api/iwallic")
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

