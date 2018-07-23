package com.iwallic.app.pages.transaction

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.utils.QRCodeUtils
import com.iwallic.app.utils.WalletUtils

class TransactionReceiveActivity : BaseActivity() {
    private lateinit var backL: LinearLayout
    private lateinit var qrIV: ImageView
    private lateinit var addressTV: TextView
    private lateinit var copyB: Button
    private var address: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(R.layout.activity_transaction_receive)
        initDOM()
        initQRCode()
        initClick()
    }

    private fun initDOM() {
        backL = findViewById(R.id.transaction_receive_back)
        qrIV = findViewById(R.id.transaction_receive_qrcode)
        addressTV = findViewById(R.id.transaction_receive_address)
        copyB = findViewById(R.id.transaction_receive_address_copy)
    }

    private fun initQRCode() {
        address = WalletUtils.address(this)
        val qrCode = QRCodeUtils.generate(address, this)
        addressTV.text = address
        if (qrCode != null) {
            qrIV.setImageBitmap(qrCode)
        }
    }

    private fun initClick() {
        backL.setOnClickListener {
            this.finish()
        }
        copyB.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("address", address)
            clipboard.primaryClip = clip
            Toast.makeText(this, R.string.error_copied, Toast.LENGTH_SHORT).show()
        }
    }
}
