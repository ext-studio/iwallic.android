package com.iwallic.app.pages.wallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.QRCodeUtils
import com.iwallic.app.utils.WalletUtils
import kotlinx.coroutines.experimental.launch
import android.content.Intent
import android.net.Uri
import java.io.File


class WalletBackupActivity : BaseActivity() {

    private var verified: Boolean = false
    private var shown: Boolean = true
    private var wif: String = ""
    private lateinit var backTV: TextView
    private lateinit var boxRL: RelativeLayout
    private lateinit var clickIV: ImageView
    private lateinit var qrIV: ImageView
    private lateinit var gateTV: TextView
    private lateinit var resultLL: LinearLayout
    private lateinit var eyeLL: LinearLayout
    private lateinit var eyeIV: ImageView
    private lateinit var eyeCloseIV: ImageView
    private lateinit var wifTV: TextView
    private lateinit var copyB: Button
    private lateinit var saveB: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_backup)
        initDOM()
        initClick()
    }

    private fun initDOM() {
        backTV = findViewById(R.id.wallet_backup_back)
        boxRL = findViewById(R.id.wallet_backup_box)
        clickIV = findViewById(R.id.wallet_backup_click)
        qrIV = findViewById(R.id.wallet_backup_qrcode)
        gateTV = findViewById(R.id.wallet_backup_gate)
        resultLL = findViewById(R.id.wallet_backup_result)
        eyeLL = findViewById(R.id.wallet_backup_toggle)
        eyeCloseIV = findViewById(R.id.wallet_backup_eye_close)
        eyeIV = findViewById(R.id.wallet_backup_eye)
        wifTV = findViewById(R.id.wallet_backup_wif)
        copyB = findViewById(R.id.wallet_backup_copy)
        saveB = findViewById(R.id.wallet_backup_save)
    }

    private fun initClick() {
        boxRL.setOnClickListener {
            if (verified) {
                return@setOnClickListener
            }
            resolveVerify()
        }
        gateTV.setOnClickListener {
            resolveVerify()
        }
        eyeLL.setOnClickListener {
            if (verified) {
                resolveToggle()
                return@setOnClickListener
            }
            resolveToggle()
        }
        copyB.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("WIF", wif)
            clipboard.primaryClip = clip
            Toast.makeText(this, R.string.error_copied, Toast.LENGTH_SHORT).show()
        }
        saveB.setOnClickListener {
//            todo export wallet as json file
//            val intent = Intent("android.intent.action.VIEW")
//            intent.addCategory("android.intent.category.DEFAULT")
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            val uri = Uri.fromFile(File(filesDir, ""))
//            intent.setDataAndType(uri, "text/plain")
            Toast.makeText(this, R.string.error_incoming, Toast.LENGTH_SHORT).show()
        }
        backTV.setOnClickListener {
            finish()
        }
    }

    private fun resolveVerify() {
        DialogUtils.password(this).subscribe{pwd ->
            if (pwd.isEmpty()) {
                return@subscribe
            }
            DialogUtils.load(this).subscribe{ load ->
                WalletUtils.verify(this, pwd).subscribe{rs ->
                    wif = rs
                    Log.i("【WalletBackup】", wif)
                    load.dismiss()
                    if (wif.isEmpty()) {
                        Toast.makeText(this, R.string.error_password, Toast.LENGTH_SHORT).show()
                    } else {
                        verified = true
                        resolveVerified()
                    }
                }
            }
        }
    }

    private fun resolveVerified() {
        gateTV.visibility = View.GONE
        clickIV.visibility = View.GONE
        val qrCode = QRCodeUtils.generate(wif, this)
        if (qrCode != null) {
            qrIV.setImageBitmap(qrCode)
            qrIV.visibility = View.VISIBLE
            boxRL.setBackgroundResource(R.drawable.shape_border_radius)
        }
        wifTV.text = wif
        resultLL.visibility = View.VISIBLE
    }

    private fun resolveToggle() {
        if (shown) {
            wifTV.text = "******************************"
            eyeIV.visibility = View.GONE
            eyeCloseIV.visibility = View.VISIBLE
            shown = false
        } else {
            wifTV.text = wif
            eyeIV.visibility = View.VISIBLE
            eyeCloseIV.visibility = View.GONE
            shown = true
        }
    }
}
