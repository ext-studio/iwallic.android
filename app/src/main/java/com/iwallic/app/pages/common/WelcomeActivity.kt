package com.iwallic.app.pages.common

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.iwallic.app.BuildConfig
import com.iwallic.app.R
import com.iwallic.app.base.BaseAuthActivity
import com.iwallic.app.models.VersionRes
import com.iwallic.app.pages.main.MainActivity
import com.iwallic.app.pages.wallet.WalletGuardActivity
import com.iwallic.app.services.DownloadService
import com.iwallic.app.states.VersionState
import com.iwallic.app.utils.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class WelcomeActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isTaskRoot) {
            finish()
            return
        }
        setContentView(R.layout.activity_welcome)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        initVersion()
    }

    private fun initVersion () {
        VersionState.check(this, true, {
            if (it != null) {
                if (it.code > BuildConfig.VERSION_CODE) {
                    resolveNewVersion(it)
                }
            }
            enter()
        }, {
            if (!DialogUtils.error(this, it)) {
                Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
            }
            enter()
        })
    }

    private fun resolveNewVersion(config: VersionRes) {
        if (config.code%2 == 0) {
            VersionState.force( this, config, {
                DownloadService.start(this, config.url)
            }, {
                exit()
            })
        } else {
            VersionState.tip(this, config, {
                DownloadService.start(this, config.url)
            }, {
                enter()
            })
        }
    }

    private fun enter() {
        launch {
            delay(1000)
            if (NeonUtils.wallet(baseContext) == null || NeonUtils.account(baseContext) == null) {
                startActivity(Intent(baseContext, WalletGuardActivity::class.java))
            } else {
                startActivity(Intent(baseContext, MainActivity::class.java))
            }
            finish()
        }
    }
    private fun exit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        } else {
            finishAffinity()
            System.exit(0)
        }
    }
}

