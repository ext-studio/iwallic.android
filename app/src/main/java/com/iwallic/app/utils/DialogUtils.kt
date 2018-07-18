package com.iwallic.app.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import com.iwallic.app.R
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import io.reactivex.Observable
import com.iwallic.app.models.BalanceRes

object DialogUtils {
    @SuppressLint("InflateParams")
    fun confirm(context: Context, title: Int? = null, body: Int? = null, ok: Int? = null, no: Int? = null): Observable<Boolean> {
        return Observable.create {observer ->
            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.dialog_confirm, null)
            val okTV = view.findViewById<TextView>(R.id.dialog_confirm_ok)
            val noTV = view.findViewById<TextView>(R.id.dialog_confirm_no)
            val titleTV = view.findViewById<TextView>(R.id.dialog_confirm_title)
            val bodyTV = view.findViewById<TextView>(R.id.dialog_confirm_body)
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

    @SuppressLint("InflateParams")
    fun password(context: Context): Observable<String> {
        return Observable.create { observer ->
            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.dialog_password, null)
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

    @SuppressLint("InflateParams")
    fun load(context: Context, text: Int = R.string.load_load): Observable<AlertDialog> {
        return Observable.create{observer ->
            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.dialog_load, null)
            builder.setView(view)
            view.findViewById<TextView>(R.id.load_title).text = inflater.context.resources.getText(text)
            val dialog = builder.create()

            dialog.show()

            observer.onNext(dialog)
            observer.onComplete()
        }
    }

    fun list(context: Context, title: Int? = null, list: List<BalanceRes> ?= null, callback: ((String) -> Unit)? = null) {
        val builder = AlertDialog.Builder(context)
        if (title != null) {
            builder.setTitle(title)
        }
        if(list != null) {
            val listValue = arrayOfNulls<String>(list.size)
            val listKey = arrayOfNulls<String>(list.size)
            for ((index, i) in list.iterator().withIndex()) {
                listKey[index] = i.assetId
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

    fun error(context: Context, code: Int): Boolean {
        when (code) {
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