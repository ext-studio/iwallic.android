package com.iwallic.app.utils

import android.app.AlertDialog
import android.content.Context
import com.iwallic.app.R
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import android.content.DialogInterface
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import io.reactivex.Observable

import android.util.Log
import android.widget.ListView
import com.iwallic.app.models.addrassets


object DialogUtils {
    fun Dialog(context: Context, title: Int? = null, body: Int? = null, ok: Int? = null, no: Int? = null, callback: ((Boolean) -> Unit)? = null) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_confirm, null)
        val okTV = view.findViewById<TextView>(R.id.dialog_confirm_ok)
        val noTV = view.findViewById<TextView>(R.id.dialog_confirm_no)
        val titleTV = view.findViewById<TextView>(R.id.dialog_confirm_title)
        val bodyTV = view.findViewById<TextView>(R.id.dialog_confirm_body)
        builder.setView(view)
        val dialog = builder.create()
        // dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window.setLayout(600, WindowManager.LayoutParams.WRAP_CONTENT)

        if (title != null) {
            titleTV.setText(title)
        }
        if (body != null) {
            bodyTV.setText(body)
        }
        if (ok != null) {
            okTV.setText(ok)
            okTV.setOnClickListener {
                if (callback != null) {
                    callback(true)
                    dialog.cancel()
                }
            }
        } else {
            okTV.visibility = View.GONE
        }
        if (no != null) {
            noTV.setText(no)
            noTV.setOnClickListener {
                if (callback != null) {
                    callback(false)
                    dialog.cancel()
                }
            }
        } else {
            noTV.visibility = View.GONE
        }
        dialog.show()
    }

    fun Password(context: Context): Observable<String> {
        return Observable.create { observer ->
            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.dialog_password, null)
            val inputET = view.findViewById<EditText>(R.id.dialog_password)
            val confirm = view.findViewById<TextView>(R.id.dialog_password_confirm)
            builder.setView(view)
            val dialog = builder.create()
            var rs: String = ""
            inputET.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    rs = p0.toString()
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })
            confirm.setOnClickListener {
                observer.onNext(rs)
                dialog.dismiss()
            }
            dialog.setOnDismissListener {
                observer.onComplete()
            }
            dialog.show()
        }
    }

    fun DialogList(context: Context, title: Int? = null, list: List<addrassets> ?= null, callback: ((String) -> Unit)? = null) {
        val builder = AlertDialog.Builder(context)
        if (title != null) {
            builder.setTitle(title)
        }
        if(list != null) {
            var listValue = arrayOfNulls<String>(list!!.size)
            var listKey = arrayOfNulls<String>(list!!.size)
            for ((index, i) in list!!.iterator().withIndex()) {
                listKey[index] = i.assetId
                listValue[index] = i.symbol
            }
            builder.setItems(listValue) { _, i ->
                if (callback != null) {
                    callback(listKey[i].toString())
                }
            }
            val dialog = builder.create()
            dialog.window.setLayout(600, 700)
            dialog.show()
        } else {
            Dialog(context, R.string.dialog_title_warn, R.string.dialog_content_noAsset, R.string.dialog_ok, null, fun (confirm: Boolean) {
                return
            })
        }
    }

    fun Error(context: Context, code: Int): Boolean {
        when (code) {
            99997, 99996 -> {
                Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show()
                return true
            }
            99995 -> {
                Toast.makeText(context, "服务器错误", Toast.LENGTH_SHORT).show()
                return true
            }
            99994 -> {
                Toast.makeText(context, "网络连接超时", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return false
    }
}