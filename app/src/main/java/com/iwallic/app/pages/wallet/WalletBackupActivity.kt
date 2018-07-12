package com.iwallic.app.pages.wallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.QRCodeUtils
import com.iwallic.app.utils.WalletUtils
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

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
    private lateinit var copyB: Button
    private lateinit var saveB: Button

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
            Toast.makeText(baseContext, R.string.error_copied, Toast.LENGTH_SHORT).show()
        }
        saveB.setOnClickListener {
            Toast.makeText(this, R.string.error_incoming, Toast.LENGTH_SHORT).show()
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
            DialogUtils.load(this) { load ->
                launch {
                    wif = WalletUtils.verify(baseContext, it)
                    load.dismiss()
                    withContext(UI) {
                        if (wif.isEmpty()) {
                            Toast.makeText(baseContext, "密码错误", Toast.LENGTH_SHORT).show()
                        } else {
                            verified = true
                            resolveVerified()
                        }
                    }
                }
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
            boxRL.setBackgroundResource(R.drawable.shape_border_radius)
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
