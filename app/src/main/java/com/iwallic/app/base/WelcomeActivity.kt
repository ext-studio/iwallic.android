package com.iwallic.app.base

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.gson.Gson
import com.iwallic.app.BuildConfig
import com.iwallic.app.R
import com.iwallic.app.models.VersionRes
import com.iwallic.app.pages.main.MainActivity
import com.iwallic.app.pages.wallet.WalletActivity
import com.iwallic.app.services.DownloadService
import com.iwallic.app.utils.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class WelcomeActivity : BaseActivity() {
    private val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_welcome)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        initNet()
        initVersion()
        initListen()

    }

    override fun onBackPressed() {
        return
    }

    private fun initListen() {
        CommonUtils.onConfigured().subscribe {
             resolveWallet()
        }
    }

    private fun initVersion () {
        HttpUtils.getPy("/client/index/app_version/detail", {
            if (it.isNotEmpty()) {
                Log.i("【WelcomeActivity】", "【$it】")
                val config = gson.fromJson(it, VersionRes::class.java)
                if (config.code > BuildConfig.VERSION_CODE) {
                    Log.i("【WelcomeActivity】", "version new【${BuildConfig.VERSION_CODE} -> ${config.name}:${config.code}】")
                    resolveNewVersion(config)
                    return@getPy
                } else {
                    FileUtils.cleanApkFile(this)
                }
                Log.i("【WelcomeActivity】", "version already latest")
            } else {
                Log.i("【WelcomeActivity】", "no version data")
            }
            CommonUtils.notifyVersion()
        }, {
            Log.i("【WelcomeActivity】", "version error【$it】")
            if (!DialogUtils.error(this, it)) {
                Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
            }
            CommonUtils.notifyVersion()
        })
    }


    private fun initNet() {
        // 101.132.97.9:45005 192.168.1.106:5000
        // request for latest config, or use local config
        CommonUtils.setNet(
                SharedPrefUtils.getNet(this),
                "http://101.132.97.9:45005", // "http://192.168.1.106:5000", // "http://101.132.97.9:45005",
                "http://101.132.97.9:8001/api/iwallic",
                "http://101.132.97.9:8002/api/iwallic")
        CommonUtils.notifyNetwork()
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val intent = Intent(this, DownloadService::class.java)
                    intent.putExtra("url", config.url)
                    startService(intent)
                } else {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(config.url))
                    startActivity(intent)
                }
            }
            CommonUtils.notifyVersion()
        }
    }

    private fun resolveWallet() {
        launch {
            delay(1000)
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

