package com.iwallic.app.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Build
import com.iwallic.app.R
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.iwallic.app.models.AssetRes
import io.reactivex.Observable
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.*

object DialogUtils {
    fun confirm(context: Context, title: Int? = null, body: Int? = null, ok: Int? = null, no: Int? = null): Observable<Boolean> {
        return Observable.create {observer ->
            val builder = AlertDialog.Builder(context)
            val view = View.inflate(context, R.layout.dialog_confirm, null)
            val okTV = view.findViewById<TextView>(R.id.dialog_confirm_ok)
            val noTV = view.findViewById<TextView>(R.id.dialog_confirm_no)
            val titleTV = view.findViewById<TextView>(R.id.dialog_confirm_title)
            val bodyTV = view.findViewById<TextView>(R.id.dialog_confirm_msg)
            builder.setView(view)
            val dialog = builder.create()

            if (title != null) {
                titleTV.setText(title)
            }
            if (body != null) {
                bodyTV.setText(body)
            }
            if (ok != null) {
                okTV.setText(ok)
                okTV.setOnClickListener {
                    observer.onNext(true)
                    observer.onComplete()
                    dialog.dismiss()
                }
            } else {
                okTV.visibility = View.GONE
            }
            if (no != null) {
                noTV.setText(no)
                noTV.setOnClickListener {
                    observer.onNext(false)
                    observer.onComplete()
                    dialog.dismiss()
                }
            } else {
                noTV.visibility = View.GONE
            }
            dialog.setOnDismissListener {
                observer.onNext(false)
                observer.onComplete()
            }
            dialog.show()
        }
    }

    fun permission(context: Context?, permission: String) {
        Toast.makeText(context, "该操作需要${when(permission) {
            Manifest.permission.CAMERA -> "相机"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "读取文件"
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> "读取文件"
            else -> ""
        }}权限，请到手机授权管理处允许以正常使用", Toast.LENGTH_SHORT).show()
    }

    fun loader(context: Context, msg: String = ""): Dialog {
        val dialog: Dialog
        val view = View.inflate(context, R.layout.dialog_loading, null)
        view.findViewById<TextView>(R.id.dialog_loading_text).text = msg
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            dialog = Dialog(context)
            dialog.setCancelable(false)
            dialog.setContentView(view)
            dialog.create()
            dialog.show()
            dialog.window.setBackgroundDrawableResource(R.color.colorTransparent)

            launch (UI) {
                delay(7000)
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
        } else {
            val builder = android.support.v7.app.AlertDialog.Builder(context)
            builder.setCancelable(false)
            builder.setView(view)
            dialog = builder.create()
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(R.color.colorTransparent)

            launch (UI) {
                delay(7000)
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
        }
        return dialog
    }

    fun confirm(context: Context, body: String, title: String = "提示", okStr: String = "确认", noStr: String = "取消"): Observable<Boolean> {
        return Observable.create { observer ->
            val view = View.inflate(context, R.layout.dialog_confirm, null)
            view.findViewById<TextView>(R.id.dialog_confirm_title).text = title
            view.findViewById<TextView>(R.id.dialog_confirm_msg).text = body
            val okTV = view.findViewById<TextView>(R.id.dialog_confirm_ok)
            val noTV = view.findViewById<TextView>(R.id.dialog_confirm_no)
            okTV.text = okStr
            noTV.text = noStr
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                val dialog = Dialog(context)
                dialog.setContentView(view)
                dialog.create()
                dialog.show()
                dialog.window.setBackgroundDrawableResource(R.color.colorTransparent)
                okTV.setOnClickListener {
                    dialog.dismiss()
                    observer.onNext(true)
                    observer.onComplete()
                }
                noTV.setOnClickListener {
                    dialog.dismiss()
                    observer.onNext(false)
                    observer.onComplete()
                }
                dialog.setOnDismissListener {
                    observer.onNext(false)
                    observer.onComplete()
                }
            } else {
                val builder = android.support.v7.app.AlertDialog.Builder(context)
                builder.setCancelable(false)
                builder.setView(view)
                val dialog = builder.create()
                dialog.show()
                dialog.window.setBackgroundDrawableResource(R.color.colorTransparent)
                okTV.setOnClickListener {
                    dialog.dismiss()
                    observer.onNext(true)
                    observer.onComplete()
                }
                noTV.setOnClickListener {
                    dialog.dismiss()
                    observer.onNext(false)
                    observer.onComplete()
                }
                dialog.setOnDismissListener {
                    observer.onNext(false)
                    observer.onComplete()
                }
            }
        }
    }

    fun password(context: Context): Observable<String> {
        return Observable.create { observer ->
            val builder = AlertDialog.Builder(context)
            val view = View.inflate(context, R.layout.dialog_password, null)
            val inputET = view.findViewById<EditText>(R.id.dialog_password)
            val confirm = view.findViewById<TextView>(R.id.dialog_password_confirm)
            builder.setView(view)
            val dialog = builder.create()
            var rs = ""
            inputET.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    rs = p0.toString()
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })
            confirm.setOnClickListener {
                observer.onNext(rs)
                observer.onComplete()
                dialog.dismiss()
            }
            dialog.setOnDismissListener {
                observer.onNext("")
                observer.onComplete()
            }
            dialog.show()
            inputET.requestFocus()
        }
    }

    fun load(context: Context, text: Int = R.string.load_load): Observable<AlertDialog> {
        return Observable.create{observer ->
            val builder = AlertDialog.Builder(context)
            val view = View.inflate(context, R.layout.dialog_load, null)
            builder.setView(view)
            view.findViewById<TextView>(R.id.load_title).text = context.resources.getText(text)
            val dialog = builder.create()

            dialog.show()

            observer.onNext(dialog)
            observer.onComplete()
        }
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
            confirm(context, R.string.dialog_title_warn, R.string.dialog_content_noAsset, R.string.dialog_ok).subscribe {}
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
}