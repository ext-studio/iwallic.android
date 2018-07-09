package com.iwallic.app.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.iwallic.app.R
import com.iwallic.app.services.BlockService
import com.iwallic.app.utils.*
import com.iwallic.app.wallet.WalletActivity
import java.util.*


open class BaseActivity : AppCompatActivity() {

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (SharedPrefUtils.getSkin(this)) {
            "default" -> setTheme(R.style.ThemeDefault)
            "night" -> setTheme(R.style.ThemeNight)
            else -> setTheme(R.style.ThemeDefault)
        }
        startService(Intent(this, BlockService::class.java))
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
}
