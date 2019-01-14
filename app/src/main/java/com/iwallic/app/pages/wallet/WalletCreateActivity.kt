package com.iwallic.app.pages.wallet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.pages.main.MainActivity
import com.iwallic.app.models.WalletModel
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.QRCodeUtils
import com.iwallic.app.utils.NeonUtils
import android.content.*
import com.iwallic.app.base.BaseAuthActivity
import com.iwallic.neon.wallet.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalletCreateActivity : BaseAuthActivity() {
    private lateinit var step1LL: LinearLayout
    private lateinit var step2LL: LinearLayout
    private lateinit var backTV: TextView
    private lateinit var pwdET: EditText
    private lateinit var confirmET: EditText
    private lateinit var errorTipTV: TextView
    private lateinit var wifTV: TextView
    private lateinit var qrCodeIV: ImageView
    private lateinit var copyTV: TextView
    private lateinit var saveTV: TextView
    private lateinit var createFL: FrameLayout
    private lateinit var enterFL: FrameLayout

    private lateinit var tipTV: TextView

    private var newWallet: WalletModel? = null
    private var newWif: String = ""
    private var pwd: String = ""
    private var confirmPwd: String = ""
    private var copied: Boolean = false
    private var saved: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_create)
        initDOM()
        initInput()
        initClick()
    }

    private fun initDOM() {
        backTV = findViewById(R.id.wallet_create_back)
        pwdET = findViewById(R.id.wallet_create_pwd)
        confirmET = findViewById(R.id.wallet_create_confirm)
        errorTipTV = findViewById(R.id.wallet_create_error)
        createFL = findViewById(R.id.wallet_create_btn_create)
        step1LL = findViewById(R.id.wallet_create_step_1)
        step2LL = findViewById(R.id.wallet_create_step_2)

        enterFL = findViewById(R.id.wallet_create_btn_enter)
        qrCodeIV = findViewById(R.id.wallet_create_qrcode)
        wifTV = findViewById(R.id.wallet_create_new_wif)
        copyTV = findViewById(R.id.wallet_create_btn_copy)
        saveTV = findViewById(R.id.wallet_create_btn_save)

        tipTV = findViewById(R.id.wallet_create_tip)
    }
    private fun initClick() {
        backTV.setOnClickListener {
            finish()
        }
        createFL.setOnClickListener {
            if (pwd.isEmpty() || confirmPwd.isEmpty()) {
                DialogUtils.toast(this, R.string.error_empty)
                return@setOnClickListener
            }
            if (pwd.length < 6 || pwd != this.confirmPwd) {
                return@setOnClickListener
            }
            resolveCreate()
        }
        enterFL.setOnClickListener {
            if (copied || saved) {
                resolveEnter()
                return@setOnClickListener
            }
            DialogUtils.confirm( this, { confirm ->
                if (confirm) {
                    resolveEnter()
                }
            }, R.string.wallet_create_dialog_enter, R.string.dialog_title_primary, R.string.dialog_ok_enter, R.string.dialog_no)
        }
        saveTV.setOnClickListener {
            DialogUtils.toast(this, R.string.error_incoming)
        }
        copyTV.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("WIF", newWif)
            clipboard.primaryClip = clip
            copied = true
            DialogUtils.toast(this, R.string.error_copied)
        }
    }
    private fun initInput() {
        pwdET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                pwd = p0.toString()
                resolveError()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        confirmET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                confirmPwd = p0.toString()
                resolveError()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }
    private fun resolveError() {
        if (pwd.length < 6) {
            errorTipTV.setText(R.string.wallet_create_error_short)
            errorTipTV.visibility = View.VISIBLE
        } else if (confirmPwd.isNotEmpty() && confirmPwd != pwd) {
            errorTipTV.setText(R.string.wallet_create_error_mismatch)
            errorTipTV.visibility = View.VISIBLE
        } else {
            errorTipTV.visibility = View.INVISIBLE
        }
    }

    private fun resolveCreate() {
        val loader = DialogUtils.loader(this, R.string.wallet_create_creating)
        GlobalScope.launch {
            var done = true
            try {
                val privateKey = Wallet.generate()
                newWallet = NeonUtils.create(pwd, privateKey)
                newWif = Wallet.priv2Wif(privateKey)
            } catch (e: Exception) {
                done = false
            }
            withContext(Dispatchers.Main) {
                loader.dismiss()
                if (done && newWallet != null && newWif.isNotEmpty()) {
                    resolveNewWallet()
                } else {
                    DialogUtils.toast(applicationContext, R.string.error_failed)
                }
            }
        }
    }
    private fun resolveNewWallet() {
        val qrCode = QRCodeUtils.generate(newWif, this)
        if (qrCode != null) {
            tipTV.setText(R.string.wallet_create_tip_new)
            step1LL.visibility = View.GONE
            step2LL.visibility = View.VISIBLE
            qrCodeIV.setImageBitmap(qrCode)
            wifTV.text = newWif
        }
    }
    private fun resolveEnter() {
        val loader = DialogUtils.loader(this)
        GlobalScope.launch {
            if (NeonUtils.save(applicationContext, newWallet!!)) {
                withContext(Dispatchers.Main) {
                    loader.dismiss()
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            } else {
                withContext(Dispatchers.Main) {
                    loader.dismiss()
                    DialogUtils.toast(applicationContext, R.string.error_failed)
                }
            }
        }
    }
}
