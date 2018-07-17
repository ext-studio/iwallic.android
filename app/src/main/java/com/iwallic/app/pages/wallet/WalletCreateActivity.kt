package com.iwallic.app.pages.wallet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.base.MainActivity
import com.iwallic.app.models.WalletModel
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.QRCodeUtils
import com.iwallic.app.utils.WalletUtils
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import android.content.*
import com.iwallic.app.base.BaseActivity
import android.app.Activity
import android.view.inputmethod.InputMethodManager
import com.iwallic.neon.wallet.Wallet


class WalletCreateActivity : BaseActivity() {
    private lateinit var step1LL: LinearLayout
    private lateinit var step2LL: LinearLayout
    private lateinit var backLL: LinearLayout
    private lateinit var pwdET: EditText
    private lateinit var confirmET: EditText
    private lateinit var createPB: ProgressBar
    private lateinit var errorTipTV: TextView
    private lateinit var wifTV: TextView
    private lateinit var qrCodeIV: ImageView
    private lateinit var copyB: Button
    private lateinit var saveB: Button
    private lateinit var createB: Button
    private lateinit var enterB: Button
    private lateinit var enterPB: ProgressBar

    var newWallet: WalletModel? = null
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

    override fun onBackPressed() {
        return
    }

    private fun initDOM() {
        backLL = findViewById(R.id.wallet_create_back)
        pwdET = findViewById(R.id.wallet_create_pwd)
        confirmET = findViewById(R.id.wallet_create_confirm)
        errorTipTV = findViewById(R.id.wallet_create_error)
        createB = findViewById(R.id.wallet_create_btn_create)
        createPB = findViewById(R.id.wallet_create_load_create)
        step1LL = findViewById(R.id.wallet_create_step_1)
        step2LL = findViewById(R.id.wallet_create_step_2)

        enterB = findViewById(R.id.wallet_create_btn_enter)
        qrCodeIV = findViewById(R.id.wallet_create_qrcode)
        wifTV = findViewById(R.id.wallet_create_new_wif)
        copyB = findViewById(R.id.wallet_create_btn_copy)
        saveB = findViewById(R.id.wallet_create_btn_save)
        enterPB = findViewById(R.id.wallet_create_load_enter)
    }
    private fun initClick() {
        backLL.setOnClickListener {
            finish()
        }
        createB.setOnClickListener {
            if (pwd.isEmpty() || confirmPwd.isEmpty()) {
                Toast.makeText(this, resources.getText(R.string.error_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pwd.length < 6 || pwd != this.confirmPwd) {
                return@setOnClickListener
            }
            resolveCreate()
        }
        enterB.setOnClickListener {
            if (copied || saved) {
                resolveEnter()
                return@setOnClickListener
            }
            DialogUtils.dialog(this, R.string.dialog_title_primary, R.string.wallet_create_dialog_enter, R.string.dialog_ok_enter, R.string.dialog_no, fun (confirm: Boolean) {
                if (confirm) {
                    resolveEnter()
                }
            })
        }
        saveB.setOnClickListener {
            Toast.makeText(baseContext, R.string.error_incoming, Toast.LENGTH_SHORT).show()
        }
        copyB.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("WIF", newWif)
            clipboard.primaryClip = clip
            copied = true
            Toast.makeText(baseContext, R.string.error_copied, Toast.LENGTH_SHORT).show()
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
        createPB.visibility = View.VISIBLE
        createB.visibility = View.INVISIBLE

        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = currentFocus
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        launch {
            var done = true
            try {
                val privateKey = Wallet.generate()
                newWallet = WalletUtils.create(pwd, privateKey)
                newWif = Wallet.priv2Wif(privateKey)
            } catch (e: Exception) {
                done = false
            }
            withContext(UI) {
                if (done) {
                    resolveNewWallet()
                } else {
                    Toast.makeText(baseContext, R.string.error_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun resolveNewWallet() {
        if (newWallet == null || newWif.isEmpty()) {
            Toast.makeText(baseContext, R.string.error_failed, Toast.LENGTH_SHORT).show()
            return
        }
        val qrCode = QRCodeUtils.generate(newWif)
        if (qrCode != null) {
            createPB.visibility = View.INVISIBLE
            createB.visibility = View.VISIBLE
            step1LL.visibility = View.GONE
            step2LL.visibility = View.VISIBLE
            qrCodeIV.setImageBitmap(qrCode)
            wifTV.text = newWif
        }
    }
    private fun resolveEnter() {
        enterPB.visibility = View.VISIBLE
        enterB.visibility = View.INVISIBLE
        launch {
            if (WalletUtils.save(baseContext, newWallet!!)) {
                val intent = Intent(baseContext, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
                return@launch
            }
            withContext(UI) {
                enterPB.visibility = View.INVISIBLE
                enterB.visibility = View.VISIBLE
                Toast.makeText(baseContext, R.string.error_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
