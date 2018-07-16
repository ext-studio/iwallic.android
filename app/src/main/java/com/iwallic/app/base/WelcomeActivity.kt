package com.iwallic.app.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import com.google.gson.Gson
import com.iwallic.app.R
import com.iwallic.app.models.OldConfig
import com.iwallic.app.pages.wallet.WalletActivity
import com.iwallic.app.utils.*
import kotlinx.coroutines.experimental.launch

class WelcomeActivity : BaseActivity() {
    private var finished: Boolean = false
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
        HttpClient.post("fetchIwallicConfig", ok = fun (res) {
            val config = gson.fromJson<OldConfig>(res, OldConfig::class.java)
            if (config?.version_android != null) {
                if (config.version_android.code != "1.0.0") {
                    DialogUtils.Dialog(
                        this,
                        R.string.dialog_title_primary,
                        R.string.dialog_version_new_body,
                        R.string.dialog_version_ok,
                        R.string.dialog_no,
                        fun (confirm) {
                            if (confirm) {
                                enterB.visibility = View.VISIBLE
                                val uri = Uri.parse(config.version_android.url)
                                startActivity(Intent(Intent.ACTION_VIEW, uri))
                            } else {
                                resolveWallet()
                            }
                        }
                    )
                    return
                }
            }
            resolveWallet()
        }, no = fun (err) {
            if (!DialogUtils.Error(baseContext, err)) {
                Toast.makeText(baseContext, "$err", Toast.LENGTH_SHORT).show()
            }
            resolveWallet()
        })
    }

    private fun resolveConfig() {
        // request for latest config, or use local config
        ConfigUtils.set(SharedPrefUtils.getNet(this), "http://101.132.97.9:8001/api/iwallic", "http://101.132.97.9:8002/api/iwallic") // , ""
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

