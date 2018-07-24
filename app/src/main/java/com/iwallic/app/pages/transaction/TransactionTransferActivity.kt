package com.iwallic.app.pages.transaction

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.iwallic.app.utils.DialogUtils
import android.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.AssetRes
import com.iwallic.app.models.TransactionModel
import com.iwallic.app.models.UtxoModel
import com.iwallic.app.states.AssetState
import com.iwallic.app.utils.HttpUtils
import com.iwallic.app.utils.WalletUtils

class TransactionTransferActivity : BaseActivity() {
    private lateinit var backLL: LinearLayout
    private lateinit var chooseAssetLL: LinearLayout
    private lateinit var targetET: EditText
    private lateinit var amountET: EditText
    private lateinit var assetNameTV: TextView
    private lateinit var balanceTV: TextView
    private lateinit var submitB: Button
    private lateinit var submitPB: ProgressBar
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

    private fun initDOM() {
        address = WalletUtils.address(this)
        backLL = findViewById(R.id.transaction_transfer_back)
        chooseAssetLL = findViewById(R.id.transaction_transfer_choose_asset)
        targetET = findViewById(R.id.transaction_transfer_target)
        amountET = findViewById(R.id.transaction_transfer_amount)
        assetNameTV = findViewById(R.id.asset_choose_name)
        balanceTV = findViewById(R.id.transaction_transfer_balance)
        submitB = findViewById(R.id.transaction_transfer_btn_submit)
        submitPB = findViewById(R.id.transaction_transfer_load_submit)
        tipTV = findViewById(R.id.transaction_transfer_error)
        successB = findViewById(R.id.transaction_transfer_success)
        step1LL = findViewById(R.id.transaction_transfer_step_1)
        step2LL = findViewById(R.id.transaction_transfer_step_2)
    }

    private fun initClick() {
        backLL.setOnClickListener {
            finish()
        }
        chooseAssetLL.setOnClickListener {
            resolveAssetPick()
        }
        submitB.setOnClickListener {
            hideKeyBoard()
            if (amount <= 0 || target.isEmpty() || amount > balance) {
                return@setOnClickListener
            }
            if (asset.isEmpty()) {
                Toast.makeText(this, R.string.transaction_transfer_tips_noChooseAsset_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            submitPB.visibility = View.VISIBLE
            submitB.visibility = View.GONE
            resolveVerify()
        }
        successB.setOnClickListener {
            finish()
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
                    amount = s.toString().toDouble()
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
        asset = intent.getStringExtra("asset") ?: ""
        val chosen = AssetState.get(asset)
        if (asset.isNotEmpty() && chosen != null) {
            assetNameTV.text = chosen.name
            balanceTV.text = resources.getString(R.string.transaction_transfer_balance_hint, chosen.balance)
            amountET.isEnabled = true
            balance = chosen.balance.toDouble()
            return
        }
        list = AssetState.cached?.filter {
            it.balance.toDouble() > 0
        }
        if (list != null && list?.isNotEmpty() == true) {
            resolveAssetPick()
        } else {
            DialogUtils.confirm(
                this,
                R.string.dialog_title_primary,
                R.string.dialog_content_nobalance,
                R.string.dialog_ok
            ).subscribe {
                finish()
            }
        }
    }

    private fun resolveSend(from: String, to: String, amount: Double) {
        if (asset.length == 64) {
            asset = "0x$asset"
        }else if (asset.length == 42) {
            asset = asset.substring(2, asset.length)
        }
        when {
            WalletUtils.check(asset, "asset") -> {
                Log.i("【transfer】", "asset tx【$asset】*【$amount】to【$target】")
                HttpUtils.post("getutxoes", listOf(from, asset), fun(res) {
                    val data = gson.fromJson<ArrayList<UtxoModel>>(res, object: TypeToken<ArrayList<UtxoModel>>() {}.type)
                    if (data == null) {
                        resolveError(99998)
                        return
                    }
                    val newTx = TransactionModel.forAsset(data, from, to, amount, asset)
                    if (newTx == null) {
                        resolveError(99699)
                        return
                    }
                    newTx.sign(wif)
                    HttpUtils.post("sendv4rawtransaction", listOf(newTx.serialize(true)), {
                        resolveSuccess(newTx.hash())
                    }, {
                        resolveError(it)
                    })
                }, fun(err) {
                    resolveError(err)
                })
            }
            WalletUtils.check(asset, "script") -> {
                Log.i("【transfer】", "token tx【$asset】*【$amount】to【$target】")
                val newTx = TransactionModel.forToken(asset, from, to, amount)
                if (newTx == null) {
                    resolveError(99699)
                    return
                }
                newTx.sign(wif)
                Log.i("【Transfer】", newTx.serialize())
                HttpUtils.post("sendv4rawtransaction", listOf(newTx.serialize(true)), fun(res) {
                    val rs: Boolean? = gson.fromJson(res, Boolean::class.java)
                    if (rs == true) {
                        resolveSuccess(newTx.hash())
                    } else {
                        resolveError(99698)
                    }
                }, fun(err) {
                    resolveError(err)
                })
            }
        }
    }
    
    private fun resolveAssetPick() {
        DialogUtils.list(this, R.string.dialog_title_choose, list, fun(confirm: String) {
            asset = confirm
            amountET.isEnabled = true
            val chooseAsset = list?.find {
                it.asset_id == confirm
            }
            assetNameTV.text = chooseAsset?.name
            balanceTV.text = resources.getString(R.string.transaction_transfer_balance_hint, chooseAsset?.balance ?: "0")
            balance = chooseAsset?.balance?.toDouble() ?: 0.0
        })
    }

    private fun resolveVerify() {
        DialogUtils.password(this).subscribe {pwd ->
            if (pwd.isEmpty()) {
                submitPB.visibility = View.GONE
                submitB.visibility = View.VISIBLE
                return@subscribe
            }
            DialogUtils.load(this).subscribe { load ->
                WalletUtils.verify(baseContext, pwd).subscribe {rs ->
                    wif = rs
                    load.dismiss()
                    if (wif.isEmpty()) {
                        hideKeyBoard()
                        resolveError(99599)
                    } else {
                        resolveSend(address, target, amount)
                    }
                }
            }
        }
    }

    private fun resolveSuccess(txid: String) {
        hideKeyBoard()
        HttpUtils.postPy(
            "/client/transaction/unconfirmed",
            mapOf(Pair("wallet_address", address), Pair("assetId", asset), Pair("txid", "0x$txid"), Pair("value", "-$amount")), {
                Log.i("【Transfer】", "submitted 【$txid】")
            }, {
                Log.i("【Transfer】", "submit failed【$it】")
            }
        )
        step1LL.visibility = View.GONE
        step2LL.visibility = View.VISIBLE
    }

    private fun resolveError(code: Int) {
        hideKeyBoard()
        if (!DialogUtils.error(this, code)) {
            Toast.makeText(this, when (code) {
                1018 -> resources.getString(R.string.transaction_transfer_error_rpc)
                99698 -> resources.getString(R.string.transaction_transfer_error_send)
                99699 -> resources.getString(R.string.transaction_transfer_error_create)
                99599 -> resources.getString(R.string.error_password)
                else -> "$code"
            }, Toast.LENGTH_SHORT).show()
        }
        submitPB.visibility = View.GONE
        submitB.visibility = View.VISIBLE
    }

    private fun resolveErrorTip() {
        when {
            !WalletUtils.check(target, "address") -> {
                tipTV.setText(R.string.transaction_transfer_tips_target_error)
                tipTV.visibility = View.VISIBLE
            }
            amount <= 0 -> {
                tipTV.setText(R.string.transaction_transfer_tips_amount_error)
                tipTV.visibility = View.VISIBLE
            }
            amount > balance -> {
                tipTV.setText(R.string.transaction_transfer_tips_amount_unenough)
                tipTV.visibility = View.VISIBLE
            }
            else -> {
                tipTV.visibility = View.GONE
            }
        }
    }
}
