package com.iwallic.app.base

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.*
import android.support.annotation.Nullable
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.iwallic.app.R
import com.iwallic.app.utils.*
import android.widget.Toast
import com.iwallic.app.pages.wallet.WalletGuardActivity

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        if (SharedPrefUtils.getAddress(this).isEmpty()) {
            val intent = Intent(this, WalletGuardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
            return
        }
        resolveTheme()
    }

    protected fun setStatusBar(space: View?) {
        if (space != null) {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            space.layoutParams.height = result
        }
    }

//    override fun attachBaseContext(base: Context) {
//        super.attachBaseContext(LocaleUtils.onAttach(base))
//    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_enter_old, R.anim.slide_leave_new)
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.slide_enter_new, R.anim.slide_leave_old)
    }

    protected fun doIfPermitted(permission: String, callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            callback()
        } else {
            DialogUtils.permission(this, permission)
        }
    }

    protected fun copy(text: String, label: String = "iwallic") {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.primaryClip = clip
        Toast.makeText(this, R.string.error_copied, Toast.LENGTH_SHORT).show()
    }

    @Suppress("DEPRECATION")
    protected fun vibrate(time: Long = 100) {
        val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibratorService.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibratorService.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibratorService.vibrate(time)
            }
        }
    }

    private fun resolveTheme() {
        when (SharedPrefUtils.getSkin(this)) {
            "default" -> {
                setStatusBarLight()
                setTheme(R.style.ThemeDefault)
            }
            "night" -> {
                setStatusBarDark()
                setTheme(R.style.ThemeNight)
            }
            else -> {
                setStatusBarLight()
                setTheme(R.style.ThemeDefault)
            }
        }
    }

    protected fun setStatusBarLight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = 0
        }
    }

    protected fun setStatusBarDark() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}
