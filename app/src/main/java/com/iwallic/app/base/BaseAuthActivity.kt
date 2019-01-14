package com.iwallic.app.base

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.utils.SharedPrefUtils

@SuppressLint("Registered")
open class BaseAuthActivity: SwipeBackActivity(true) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        resolveTheme()
    }

//    override fun attachBaseContext(base: Context) {
//        super.attachBaseContext(LocaleUtils.onAttach(base))
//    }

//    override fun finish() {
//        super.finish()
//        overridePendingTransition(R.anim.slide_enter_old, R.anim.slide_leave_new)
//    }
//
//    override fun startActivity(intent: Intent?) {
//        super.startActivity(intent)
//        overridePendingTransition(R.anim.slide_enter_new, R.anim.slide_leave_old)
//    }

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

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (isShouldHideKeyboard(v, event)) {
                hideKeyboard(v!!.windowToken)
            }
        }
        return super.dispatchTouchEvent(event)
    }
    private fun isShouldHideKeyboard(v: View?, event: MotionEvent): Boolean {
        if (v != null && v is EditText) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            val right = left + v.getWidth()
            return !(event.x > left && event.x < right
                    && event.y > top && event.y < bottom)
        }
        return false
    }

    private fun hideKeyboard(token: IBinder?) {
        if (token != null) {
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
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
