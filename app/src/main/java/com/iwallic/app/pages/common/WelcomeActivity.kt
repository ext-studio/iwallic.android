package com.iwallic.app.pages.common

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.iwallic.neon.hex.Hex
import com.iwallic.neon.utils.HEX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WelcomeActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isTaskRoot) {
            finish()
            return
        }
        setContentView(R.layout.activity_welcome)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        if (CommonUtils.debug) {
            enter()
        } else {
            initVersion()
        }
    }

    private fun initVersion () {
        VersionState.check(this, true, {
            if (it != null) {
                if (it.code > BuildConfig.VERSION_CODE) {
                    resolveNewVersion(it)
                    return@check
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
                Toast.makeText(this, R.string.welcome_updating, Toast.LENGTH_SHORT).show()
            }, {
                exit()
            })
        } else {
            VersionState.tip(this, config, {
                DownloadService.start(this, config.url)
                Toast.makeText(this, R.string.welcome_updating, Toast.LENGTH_SHORT).show()
                enter()
            }, {
                enter()
            })
        }
    }

    private fun enter() {
        GlobalScope.launch(Dispatchers.Main) {
            delay(1000L)
            if (NeonUtils.wallet(applicationContext) == null || NeonUtils.account(applicationContext) == null) {
                startActivity(Intent(applicationContext, WalletGuardActivity::class.java))
            } else {
                startActivity(Intent(applicationContext, MainActivity::class.java))
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

    private fun initTest() {
//        Log.i("HEX", "fromVarInt【${HEX.fromVarInt(635928008)}】")
        Log.i("【】", "【${Hex.toFixedNum(10000000.0, 8)}】")
//        Log.i("HEX", "reverse【${HEX.reverse("0xd1b9428a99f7805e4bbfa0f2805bc558e0e48de5")}】")
//        Log.i("HEX", "reverse【${HEX.reverse("d1b9428a99f7805e4bbfa0f2805bc558e0e48de5")}】")
//        Log.i("HEX", "toFixedNum【${HEX.toFixedNum(0.1)}】")
        Log.i("HEX", "toFixedNum【${HEX.toFixedNum(10000000.0)}】")
//        Log.i("HEX", "fromString【${HEX.fromString("Hello~")}】")
//        Log.i("HEX", "int2HexInt【${HEX.int2HexInt(10000000)}】")
//        Log.i("HEX", "xor【${HEX.xor("e0e48de5", "d1b9428a")}】")
//        Log.i("HEX", "hash256【${HEX.hash256("d1b9428a99f7805e4bbfa0f2805bc558e0e48de5")}】")
    }
}

