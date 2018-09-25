package com.iwallic.app.pages.transaction

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import android.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.integration.android.IntentIntegrator
import com.iwallic.app.models.AssetRes
import com.iwallic.app.models.TransactionModel
import com.iwallic.app.models.UtxoModel
import com.iwallic.app.states.AssetState
import com.iwallic.app.states.UnconfirmedState
import com.iwallic.app.utils.*

class TransactionTransferActivity : BaseActivity() {
    private lateinit var backLL: TextView
    private lateinit var chooseAssetLL: LinearLayout
    private lateinit var targetET: EditText
    private lateinit var amountET: EditText
    private lateinit var assetNameTV: TextView
    private lateinit var balanceTV: TextView
    private lateinit var scanIV: ImageView
    private lateinit var submitFL: FrameLayout
    private lateinit var tipTV: TextView
    private lateinit var successB: Button
    private lateinit var step1LL: LinearLayout
    private lateinit var step2LL: LinearLayout
    private val gson = Gson()
    private var wif: String = ""
    private var asset: String = ""
    private var target: String = ""
    private var address: String = ""
    private var amount = 0.0
    private var balance = 0.0
    private var list: List<AssetRes>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_transfer)
        initDOM()
        initClick()
        initInput()
        initAsset()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                Log.i("【transfer】", "scanned【${result.contents}】")
                if (NeonUtils.check(result.contents, "address")) {
                    target = result.contents
                    targetET.setText(result.contents)
                } else {
                    Toast.makeText(this, R.string.transaction_transfer_scan_error, Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.i("【transfer】", "scan cancelled")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    private fun initDOM() {
        address = NeonUtils.address(this)
        backLL = findViewById(R.id.transaction_transfer_back)
        chooseAssetLL = findViewById(R.id.transaction_transfer_choose_asset)
        targetET = findViewById(R.id.transaction_transfer_target)
        amountET = findViewById(R.id.transaction_transfer_amount)
        assetNameTV = findViewById(R.id.asset_choose_name)
        balanceTV = findViewById(R.id.transaction_transfer_balance)
        submitFL = findViewById(R.id.transaction_transfer_btn_submit)
        scanIV = findViewById(R.id.transaction_transfer_scan)
        tipTV = findViewById(R.id.transaction_transfer_error)
        successB = findViewById(R.id.transaction_transfer_success)
        step1LL = findViewById(R.id.transaction_transfer_step_1)
        step2LL = findViewById(R.id.transaction_transfer_step_2)
        setStatusBar(findViewById(R.id.app_top_space))
    }

    private fun initClick() {
        backLL.setOnClickListener {
            finish()
        }
        chooseAssetLL.setOnClickListener {
            resolveAssetPick()
        }
        submitFL.setOnClickListener {
            if (amount <= 0 || target.isEmpty() || amount > balance) {
                return@setOnClickListener
            }
            if (asset.isEmpty()) {
                Toast.makeText(this, R.string.transaction_transfer_tips_noChooseAsset_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            resolveVerify()
        }
        successB.setOnClickListener {
            finish()
        }
        scanIV.setOnClickListener {
            val scan = IntentIntegrator(this)
            scan.setOrientationLocked(true)
            scan.initiateScan()
        }
    }

    private fun initInput() {
        amountET.isEnabled = false
        amountET.setOnFocusChangeListener {_, hasFocus ->
            if(hasFocus) {
                balanceTV.visibility = View.VISIBLE
            } else {
                balanceTV.visibility = View.GONE
            }
        }
        amountET.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) {
                    amount = try {
                        s.toString().toDouble()
                    } catch (_: Throwable) {
                        0.0
                    }
                }
                resolveErrorTip()
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })
        targetET.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                target = s.toString()
                resolveErrorTip()
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })
    }

    private fun initAsset() {
        list = AssetState.cached?.filter {
            it.balance.toDouble() > 0
        }
        asset = intent.getStringExtra("asset") ?: ""
        val chosen = AssetState.get(asset)
        if (asset.isNotEmpty() && chosen != null) {
            assetNameTV.text = chosen.symbol
            balanceTV.text = resources.getString(R.string.transaction_transfer_balance_hint, chosen.balance)
            amountET.isEnabled = true
            balance = chosen.balance.toDouble()
            return
        }
        if (list != null && list?.isNotEmpty() == true) {
            resolveAssetPick()
        } else {
            DialogUtils.confirm(this, {
                finish()
            }, R.string.dialog_content_nobalance, R.string.dialog_title_primary, R.string.dialog_ok)
        }
    }

    private fun resolveSend(from: String, to: String, amount: Double) {
        val loader = DialogUtils.loader(this, R.string.transaction_transfer_sending)
        if (asset.length == 64) {
            asset = "0x$asset"
        }else if (asset.length == 42) {
            asset = asset.substring(2, asset.length)
        }
        when {
            NeonUtils.check(asset, "asset") -> {
                Log.i("【transfer】", "asset tx【$asset】*【$amount】to【$target】")
                NeonUtils.fetchBalance(this, from, asset, { balance ->
                    val newTx = TransactionModel.forAsset(balance, from, to, amount, asset)
                    if (newTx == null) {
                        loader.dismiss()
                        resolveError(99699)
                    } else {
                        newTx.sign(wif)
                        HttpUtils.post(this, "sendv4rawtransaction", listOf(newTx.serialize(true)), {
                            loader.dismiss()
                            resolveSuccess(newTx.hash())
                        }, {
                            loader.dismiss()
                            resolveError(it)
                        })
                    }
                }, {
                    loader.dismiss()
                    resolveError(it)
                })
            }
            NeonUtils.check(asset, "script") -> {
                Log.i("【transfer】", "token tx【$asset】*【$amount】to【$target】")
                val newTx = TransactionModel.forToken(asset, from, to, amount)
                if (newTx == null) {
                    loader.dismiss()
                    resolveError(99699)
                    return
                }
                newTx.sign(wif)
                HttpUtils.post(this, "sendv4rawtransaction", listOf(newTx.serialize(true)), fun(res) {
                    loader.dismiss()
                    val rs: Boolean? = gson.fromJson(res, Boolean::class.java)
                    if (rs == true) {
                        resolveSuccess(newTx.hash())
                    } else {
                        resolveError(99698)
                    }
                }, fun(err) {
                    loader.dismiss()
                    resolveError(err)
                })
            }
        }
    }
    
    private fun resolveAssetPick() {
        DialogUtils.list(this, R.string.dialog_title_choose, list) { confirm: String ->
            asset = confirm
            amountET.isEnabled = true
            val chooseAsset = list?.find {
                it.asset_id == confirm
            }
            assetNameTV.text = chooseAsset?.symbol
            balanceTV.text = resources.getString(R.string.transaction_transfer_balance_hint, chooseAsset?.balance ?: "0")
            balance = chooseAsset?.balance?.toDouble() ?: 0.0
        }
    }

    private fun resolveVerify() {
        DialogUtils.password(this) {pwd ->
            if (pwd.isEmpty()) {
                return@password
            }
            val loader = DialogUtils.loader(this, R.string.transaction_transfer_verifying)
            NeonUtils.verify(baseContext, pwd, {
                wif = it
                loader.dismiss()
                if (wif.isEmpty()) {
                    resolveError(99599)
                } else {
                    resolveSend(address, target, amount)
                }
            }, {
                loader.dismiss()
                resolveError(99999)
            })
        }
    }

    private fun resolveSuccess(txid: String) {
        if (SharedPrefUtils.getNet(this) == "main") {
            HttpUtils.postPy(
                this,
                "/client/transaction/unconfirmed",
                mapOf(Pair("wallet_address", address), Pair("asset_id", asset), Pair("txid", "0x$txid"), Pair("value", "-$amount")
            ), {
                Log.i("【Transfer】", "submitted 【$txid】")
//                TxBroadCast.send(this, txid)
                UnconfirmedState.clear(this)
            }, {
                Log.i("【Transfer】", "submit failed【$it】")
            })
        }
        step1LL.visibility = View.GONE
        step2LL.visibility = View.VISIBLE
    }

    private fun resolveError(code: Int) {
        if (!DialogUtils.error(this, code)) {
            Toast.makeText(this, when (code) {
                1018 -> resources.getString(R.string.transaction_transfer_error_rpc)
                99698 -> resources.getString(R.string.transaction_transfer_error_send)
                99699 -> resources.getString(R.string.transaction_transfer_error_create)
                99599 -> resources.getString(R.string.error_password)
                else -> "$code"
            }, Toast.LENGTH_SHORT).show()
        }
    }

    private fun resolveErrorTip() {
        when {
            target.isNotEmpty() && !NeonUtils.check(target, "address") -> {
                tipTV.setText(R.string.transaction_transfer_tips_target_error)
                tipTV.visibility = View.VISIBLE
            }
            amount <= 0 && amountET.text.isNotEmpty() -> {
                tipTV.setText(R.string.transaction_transfer_tips_amount_error)
                tipTV.visibility = View.VISIBLE
            }
            amount > 0 && amount > balance -> {
                tipTV.setText(R.string.transaction_transfer_tips_amount_unenough)
                tipTV.visibility = View.VISIBLE
            }
            else -> {
                tipTV.visibility = View.GONE
            }
        }
    }
}
