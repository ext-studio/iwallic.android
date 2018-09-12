package com.iwallic.app.pages.common

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
import com.iwallic.app.base.BaseActivity
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
        if (!isTaskRoot) {
            finish()
            return
        }
        setContentView(R.layout.activity_base_welcome)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        initVersion()
        initListen()

    }

    private fun initListen() {
        CommonUtils.onConfigured().subscribe {
             resolveWallet()
        }
    }

    private fun initVersion () {
        HttpUtils.getPy(this, "/client/index/app_version/detail", {
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

    private fun resolveNewVersion(config: VersionRes) {
        Log.i("【WelcomeActivity】", "new version")
        DialogUtils.update(
            this,
            (config.info["cn"] as String).replace("\\n", "\n"),
            (config.info["en"] as String).replace("\\n", "\n")
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

