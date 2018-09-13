package com.iwallic.app.pages.user

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.iwallic.app.BuildConfig
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.models.VersionRes
import com.iwallic.app.pages.common.BrowserActivity
import com.iwallic.app.services.DownloadService
import com.iwallic.app.states.VersionState
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.HttpUtils

class UserAboutActivity : BaseActivity() {
    private lateinit var backTV: TextView
    private lateinit var disclamerFL: FrameLayout
    private lateinit var versionFL: FrameLayout
    private lateinit var versionTV: TextView
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_about)

        initDOM()
        initClick()
    }

    private fun initDOM() {
        backTV = findViewById(R.id.user_about_back)
        disclamerFL = findViewById(R.id.activity_user_about_disclaimer)
        versionFL = findViewById(R.id.activity_user_about_version)
        versionTV = findViewById(R.id.activity_user_about_version_name)

        versionTV.text = BuildConfig.VERSION_NAME
    }

    private fun initClick() {
        disclamerFL.setOnClickListener {
            val intent = Intent(this, BrowserActivity::class.java)
            intent.putExtra("url", "https://iwallic.com/assets/disclaimer")
            startActivity(intent)
        }
        versionFL.setOnClickListener {
            initVersion()
        }
        backTV.setOnClickListener {
            finish()
        }
    }

    private fun initVersion () {
        VersionState.check(this, true).subscribe({
            if (it != null) {
                if (it.code > BuildConfig.VERSION_CODE) {
                    resolveNewVersion(it)
                    return@subscribe
                }
            }
        }, {
            val code = try {it.message?.toInt() ?: 99999}catch (_: Throwable) {99999}
            if (!DialogUtils.error(this, code)) {
                Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun resolveNewVersion(config: VersionRes) {
        if (config.code%2 == 0) {
            VersionState.force( this, config, {
                DownloadService.start(this, config.url)
            }, {})
        } else {
            VersionState.tip(this, config, {
                DownloadService.start(this, config.url)
            }, {})
        }
    }
}
