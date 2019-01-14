package com.iwallic.app.utils

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Build
import com.iwallic.app.R
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.view.WindowManager
import android.widget.EditText
import com.iwallic.app.models.AssetRes
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object DialogUtils {
    private var toaster: Toast? = null

    fun confirm(context: Context?, callback: ((Boolean) -> Unit)?, body: Any, title: Any? = null, ok: Any? = null, no: Any? = null) {
        if (context == null) {
            return
        }
        val dialog = create(context, R.layout.dialog_confirm)
        val titleTV = dialog.findViewById<TextView>(R.id.dialog_confirm_title)
        val bodyTV = dialog.findViewById<TextView>(R.id.dialog_confirm_body)
        val okTV = dialog.findViewById<TextView>(R.id.dialog_confirm_ok)
        val noTV = dialog.findViewById<TextView>(R.id.dialog_confirm_no)
        val lineBtn = dialog.findViewById<View>(R.id.dialog_confirm_line_btn)
        when (body) {
            is String -> bodyTV.text = body
            is SpannableString -> bodyTV.text = body
            is Int -> bodyTV.setText(body)
            else -> bodyTV.visibility = View.GONE
        }
        when (title) {
            is String -> titleTV.text = title
            is Int -> titleTV.setText(title)
            else -> {
                titleTV.visibility = View.GONE
                dialog.findViewById<View>(R.id.dialog_confirm_line_title).visibility = View.GONE
            }
        }
        var noBtn: Boolean = false
        when (ok) {
            is String -> okTV.text = ok
            is Int -> okTV.setText(ok)
            else -> {
                okTV.visibility = View.GONE
                lineBtn.visibility = View.GONE
                noBtn = true
            }
        }
        when (no) {
            is String -> noTV.text = no
            is Int -> noTV.setText(no)
            else -> {
                noTV.visibility = View.GONE
                lineBtn.visibility = View.GONE
                if (noBtn) {
                    dialog.findViewById<View>(R.id.dialog_confirm_line_body).visibility = View.GONE
                }
            }
        }
        var confirm = false
        okTV.setOnClickListener {
            confirm = true
            dialog.dismiss()
            callback?.invoke(true)
        }
        noTV.setOnClickListener {
            confirm = false
            dialog.dismiss()
        }
        dialog.setOnDismissListener {
            if (!confirm) {
                callback?.invoke(false)
            }
        }
    }

    fun permission(context: Context?, permission: String) {
        toast(context, R.string.error_failed) // todo tell which permission not granted
//        Toast.makeText(context, "该操作需要${when(permission) {
//            Manifest.permission.CAMERA -> "相机"
//            Manifest.permission.READ_EXTERNAL_STORAGE -> "读取文件"
//            Manifest.permission.WRITE_EXTERNAL_STORAGE -> "读取文件"
//            else -> ""
//        }}权限，请到手机授权管理处允许以正常使用", Toast.LENGTH_SHORT).show()
    }

    fun toast(context: Context?, msg: String): Toast? {
        toaster?.cancel()
        toaster = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
        toaster?.show()
        return toaster
    }

    fun toast(context: Context?, msgRes: Int): Toast? {
        toaster?.cancel()
        toaster = Toast.makeText(context, msgRes, Toast.LENGTH_SHORT)
        toaster?.show()
        return toaster
    }

    fun loader(context: Context, msg: String = "", autoClose: Boolean = false): Dialog {
        val dialog: Dialog
        val view = View.inflate(context, R.layout.dialog_loading, null)
        view.findViewById<TextView>(R.id.dialog_loading_text).text = msg
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            dialog = Dialog(context)
            dialog.setCancelable(false)
            dialog.setContentView(view)
            dialog.create()
        } else {
            val builder = android.support.v7.app.AlertDialog.Builder(context)
            builder.setCancelable(false)
            builder.setView(view)
            dialog = builder.create()
        }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.color.colorTransparent)

        if (autoClose) {
            GlobalScope.launch (Dispatchers.Main) {
                delay(10000L)
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
        }
        return dialog
    }

    fun loader(context: Context, msgRes: Int, autoClose: Boolean = false): Dialog {
        val dialog: Dialog
        val view = View.inflate(context, R.layout.dialog_loading, null)
        view.findViewById<TextView>(R.id.dialog_loading_text).setText(msgRes)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            dialog = Dialog(context)
            dialog.setCancelable(false)
            dialog.setContentView(view)
            dialog.create()
        } else {
            val builder = android.support.v7.app.AlertDialog.Builder(context)
            builder.setCancelable(false)
            builder.setView(view)
            dialog = builder.create()
        }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.color.colorTransparent)

        if (autoClose) {
            GlobalScope.launch (Dispatchers.Main) {
                delay(10000L)
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
        }
        return dialog
    }

    fun password(context: Context, ok: (String) -> Unit) {
        val view = View.inflate(context, R.layout.dialog_password, null)
        val inputET = view.findViewById<EditText>(R.id.dialog_password)
        val confirm = view.findViewById<TextView>(R.id.dialog_password_confirm)
        var rs = ""
        inputET.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                rs = p0.toString()
                confirm.isEnabled = rs.isNotEmpty()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        val dialog: Dialog
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            dialog = Dialog(context)
            dialog.setContentView(view)
            dialog.create()
        } else {
            val builder = AlertDialog.Builder(context)
            builder.setView(view)
            dialog = builder.create()
        }

        confirm.setOnClickListener {
            ok(rs)
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dialog.window?.setBackgroundDrawableResource(R.color.colorTransparent)

        inputET?.requestFocus()
    }

    fun list(context: Context, title: Int? = null, list: List<AssetRes> ?= null, callback: ((String) -> Unit)? = null) {
        val builder = AlertDialog.Builder(context)
        if (title != null) {
            builder.setTitle(title)
        }
        if(list != null) {
            val listValue = arrayOfNulls<String>(list.size)
            val listKey = arrayOfNulls<String>(list.size)
            for ((index, i) in list.iterator().withIndex()) {
                listKey[index] = i.asset_id
                listValue[index] = i.symbol
            }
            builder.setItems(listValue) { _, i ->
                if (callback != null) {
                    callback(listKey[i].toString())
                }
            }
            val dialog = builder.create()
            dialog.show()
        } else {
            confirm(context, null, R.string.dialog_title_warn, R.string.dialog_content_noAsset, R.string.dialog_ok)
        }
    }

    fun error(context: Context?, code: Int): Boolean {
        when (code) {
            1013 -> {
                Toast.makeText(context, R.string.error_rpc, Toast.LENGTH_SHORT).show()
                return true
            }
            99998 -> {
                Toast.makeText(context, R.string.error_parse, Toast.LENGTH_SHORT).show()
                return true
            }
            99997, 99996 -> {
                Toast.makeText(context, R.string.error_request, Toast.LENGTH_SHORT).show()
                return true
            }
            100000, 99995 -> {
                Toast.makeText(context, R.string.error_server, Toast.LENGTH_SHORT).show()
                return true
            }
            99994 -> {
                Toast.makeText(context, R.string.error_timeout, Toast.LENGTH_SHORT).show()
                return true
            }
            99599 -> {
                Toast.makeText(context, R.string.error_password, Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return false
    }

    /*
    create compatible dialog with layout
     */
    private fun create(context: Context, layout: Int): Dialog {
        val dialog: Dialog
        val view = View.inflate(context, layout, null)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            dialog = Dialog(context)
            dialog.setContentView(view)
            dialog.create()
        } else {
            val builder = android.support.v7.app.AlertDialog.Builder(context)
            builder.setView(view)
            dialog = builder.create()
        }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.color.colorTransparent)
        return dialog
    }
}