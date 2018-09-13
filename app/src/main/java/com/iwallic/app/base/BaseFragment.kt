package com.iwallic.app.base

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.iwallic.app.R
import android.view.Window.ID_ANDROID_CONTENT
import android.widget.LinearLayout
import com.iwallic.app.utils.CommonUtils
import com.iwallic.app.utils.DialogUtils


open class BaseFragment: Fragment() {
    protected fun setStatusBarSpace(view: View) {
        val spaceV = view.findViewById<LinearLayout>(R.id.app_top_space)
        if (spaceV != null) {
            spaceV.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonUtils.getStatusBarHeight(view.context).toInt())
        }
    }

    protected fun copy(text: String, label: String = "iwallic") {
        val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.primaryClip = clip
        Toast.makeText(context, R.string.error_copied, Toast.LENGTH_SHORT).show()
    }

    protected fun doIfPermitted(permission: String, callback: () -> Unit) {
        if(context == null ) {
            return
        }
        if (ContextCompat.checkSelfPermission(context!!, permission) == PackageManager.PERMISSION_GRANTED) {
            callback()
        } else {
            DialogUtils.permission(context, permission)
        }
    }

    @Suppress("DEPRECATION")
    protected fun vibrate(time: Long = 100) {
        val vibratorService = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibratorService.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibratorService.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibratorService.vibrate(time)
            }
        }
    }
}
