package com.iwallic.app.pages.wallet

import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.QRCodeUtils
import com.iwallic.app.utils.WalletUtils

class WalletBackupActivity : BaseActivity() {

    private var verified: Boolean = false
    private var shown: Boolean = true
    private var wif: String = ""
    private lateinit var backLL: LinearLayout
    private lateinit var boxRL: RelativeLayout
    private lateinit var clickIV: ImageView
    private lateinit var qrIV: ImageView
    private lateinit var gateTV: TextView
    private lateinit var resultLL: LinearLayout
    private lateinit var eyeLL: LinearLayout
    private lateinit var eyeIV: ImageView
    private lateinit var wifTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_backup)
        initDOM()
        initClick()
    }

    private fun initDOM() {
        backLL = findViewById(R.id.wallet_backup_back)
        boxRL = findViewById(R.id.wallet_backup_box)
        clickIV = findViewById(R.id.wallet_backup_click)
        qrIV = findViewById(R.id.wallet_backup_qrcode)
        gateTV = findViewById(R.id.wallet_backup_gate)
        resultLL = findViewById(R.id.wallet_backup_result)
        eyeLL = findViewById(R.id.wallet_backup_toggle)
        eyeIV = findViewById(R.id.wallet_backup_eye)
        wifTV = findViewById(R.id.wallet_backup_wif)
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

        backLL.setOnClickListener {
            finish()
        }
    }

    private fun resolveVerify() {
        DialogUtils.Password(this).subscribe {
            if (it.isEmpty()) {
                return@subscribe
            }
            // todo need progress bar here
            wif = WalletUtils.verify(this, it)
            if (wif.isEmpty()) {
                Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show()
            } else {
                verified = true
                resolveVerified()
            }
        }
    }

    private fun resolveVerified() {
        gateTV.visibility = View.GONE
        clickIV.visibility = View.GONE
        val qrCode = QRCodeUtils.Generate(wif)
        if (qrCode != null) {
            qrIV.setImageBitmap(qrCode)
            qrIV.visibility = View.VISIBLE
        }
        wifTV.text = wif
        resultLL.visibility = View.VISIBLE
    }

    private fun resolveToggle() {
        if (shown) {
            wifTV.text = "******************************"
            eyeIV.setImageResource(R.drawable.icon_eye_close)
            shown = false
        } else {
            wifTV.text = wif
            eyeIV.setImageResource(R.drawable.icon_eye)
            shown = true
        }
    }
}
