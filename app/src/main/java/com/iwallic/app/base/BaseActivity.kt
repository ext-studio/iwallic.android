package com.iwallic.app.base

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.iwallic.app.R
import com.iwallic.app.services.BlockService
import com.iwallic.app.utils.*


open class BaseActivity : AppCompatActivity() {

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resolveTheme()
        ConfigUtils.listen().subscribe({
            if (it) {
                startService(Intent(this, BlockService::class.java))
            }
        }, {
            Log.i("基活动", "配置失败，服务将不启动")
        })
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleUtils.OnAttach(base))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_leave_this, R.anim.slide_leave_that)
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.slide_enter_this, R.anim.slide_enter_that)
    }

    private fun resolveTheme() {
        when (SharedPrefUtils.getSkin(this)) {
            "default" -> {
                setTheme(R.style.ThemeDefault)
            }
            "night" -> {
                setTheme(R.style.ThemeNight)
            }
            else -> {
                setTheme(R.style.ThemeDefault)
            }
        }
    }
}
