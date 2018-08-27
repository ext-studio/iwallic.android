package com.iwallic.app.pages.user

import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.iwallic.app.BuildConfig
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.models.VersionRes
import com.iwallic.app.services.DownloadService
import com.iwallic.app.utils.CommonUtils
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
            val intent = Intent(this, UserBrowserActivity::class.java)
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
        HttpUtils.getPy("/client/index/app_version/detail", {
            if (it.isNotEmpty()) {
                Log.i("【WelcomeActivity】", "【$it】")
                val config = gson.fromJson(it, VersionRes::class.java)
                if (config.code > BuildConfig.VERSION_CODE) {
                    Log.i("【WelcomeActivity】", "version new【${BuildConfig.VERSION_CODE} -> ${config.name}:${config.code}】")
                    resolveNewVersion(config)
                    return@getPy
                }
                Toast.makeText(this, R.string.error_version_latest, Toast.LENGTH_SHORT).show()
                Log.i("【WelcomeActivity】", "version already latest")
            } else {
                Log.i("【WelcomeActivity】", "no version data")
            }
        }, {
            Log.i("【WelcomeActivity】", "version error【$it】")
            if (!DialogUtils.error(this, it)) {
                Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
            }
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
        }
    }
}
