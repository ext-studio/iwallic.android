package com.iwallic.app.pages.transaction

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.utils.QRCodeUtils
import com.iwallic.app.utils.WalletUtils

class TransactionReceiveActivity : BaseActivity() {

    private var backL: LinearLayout ?= null
    private var qrIV: ImageView ?= null
    private var addressTV: TextView ?= null
    private var copyB: Button ?= null
    private var address: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(R.layout.activity_transaction_receive)
        address = WalletUtils.address(this)
        this.initDOM()
        this.initClick()
    }

    private fun initDOM() {
        backL = findViewById(R.id.transaction_receive_back)
        qrIV = findViewById(R.id.transaction_receive_qrcode)
        addressTV = findViewById(R.id.transaction_receive_address)
        copyB = findViewById(R.id.transaction_receive_address_copy)
        val qrCode = QRCodeUtils.Generate(address)
        addressTV?.text = address
        if (qrCode != null) {
            qrIV?.setImageBitmap(qrCode)
        }
    }

    private fun initClick() {
        backL?.setOnClickListener {
            this.finish()
        }
        copyB?.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("address", address)
            clipboard.primaryClip = clip
            Toast.makeText(this, R.string.error_copied, Toast.LENGTH_SHORT).show()
        }
    }
}
