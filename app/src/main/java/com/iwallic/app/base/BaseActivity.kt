package com.iwallic.app.base

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.*
import android.support.annotation.Nullable
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.iwallic.app.R
import com.iwallic.app.services.BlockService
import com.iwallic.app.utils.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {
    private lateinit var mInputMethodManager: InputMethodManager

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val space = findViewById<View>(R.id.app_top_space)
        if (space != null) {
            val rectangle = Rect()
            window.decorView.getWindowVisibleDisplayFrame(rectangle)
            val statusBarHeight = rectangle.top
            val contentViewTop = window.findViewById<View>(Window.ID_ANDROID_CONTENT).top
            val titleBarHeight = contentViewTop - statusBarHeight
            space.layoutParams = ViewGroup.LayoutParams(0, titleBarHeight)
        }
        resolveTheme()
        CommonUtils.onConfigured().subscribe({
            if (it) {
                startService(Intent(this, BlockService::class.java))
            }
        }, {
            Log.i("【BaseActivity】", "config failed, block service will not on")
        })
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

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleUtils.onAttach(base))
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

    protected fun hideKeyBoard() {
        val view = currentFocus
        if (view != null) {
            val inputMethodManager = mInputMethodManager
            val active = inputMethodManager.isActive
            if (active) {
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            }
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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v!!.windowToken)
            }
        }
        return super.dispatchTouchEvent(ev)
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
}
