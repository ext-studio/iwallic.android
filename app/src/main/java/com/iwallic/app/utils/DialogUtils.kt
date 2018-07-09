package com.iwallic.app.utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.iwallic.app.R
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView

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
}