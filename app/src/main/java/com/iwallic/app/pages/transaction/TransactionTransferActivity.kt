package com.iwallic.app.pages.transaction

import android.content.Context
import android.os.Bundle
import android.renderscript.Sampler
import android.util.Log
import android.view.View
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.iwallic.app.utils.DialogUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.TransactionModel
import com.iwallic.app.models.UtxoModel
import com.iwallic.app.models.addrassets
import com.iwallic.app.states.AssetState
import com.iwallic.app.utils.HttpClient
import com.iwallic.app.utils.WalletUtils
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import org.w3c.dom.Text

class TransactionTransferActivity : BaseActivity() {

    private var backL: LinearLayout ?= null
    private var chooseAssetR: RelativeLayout ?= null
    private var targetTV: EditText ?= null
    private var amountTV: EditText ?= null
    private var assetNameTV: TextView ?= null
    private var balanceTV: TextView ?= null
    private var submitBT: Button ?= null
    private var submitPB: ProgressBar ?= null
    private var tipTV: TextView ?= null
    private val gson = Gson()
    private var wif: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_transfer)

        this.initDOM()
        this.initClick()
        this.initInput()
    }

    private fun initDOM() {
        backL = findViewById(R.id.transaction_transfer_back)
        chooseAssetR = findViewById(R.id.transaction_transfer_choose_asset)
        targetTV = findViewById(R.id.transaction_transfer_target)
        amountTV = findViewById(R.id.transaction_transfer_amount)
        assetNameTV = findViewById(R.id.asset_choose_name)
        balanceTV = findViewById(R.id.transaction_transfer_balance)
        submitBT = findViewById(R.id.transaction_transfer_btn_submit)
        submitPB = findViewById(R.id.transaction_transfer_load_submit)
        tipTV = findViewById(R.id.transaction_transfer_error)
    }

    private fun initClick() {
        var chooseAssetId = ""
        var address = WalletUtils.address(this)
        var target = ""
        var amount: Float = 0F
        backL!!.setOnClickListener {
            this.finish()
        }
        chooseAssetR!!.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
            val list = AssetState.cached?.filter {
                true
            }
            DialogUtils.DialogList(this, R.string.dialog_title_choose, list, fun(confirm: String) {
                chooseAssetId = confirm
                amountTV?.isEnabled = true
                val chooseAsset = list?.find {
                    it.assetId == confirm
                }
                assetNameTV!!.text = chooseAsset?.symbol
                balanceTV?.text ="${resources.getText(R.string.transaction_transfer_balance_hint)}: ${chooseAsset?.balance}"
            })
        }

        submitBT!!.setOnClickListener {
            target = this.targetTV?.text.toString()
            if(amountTV?.text.toString() != "") {
                amount = amountTV?.text.toString().toFloat()
            }
            if(chooseAssetId === "") {
                this.tipTV?.text = resources.getText(R.string.transaction_transfer_tips_noChooseAsset_error)
                this.tipTV?.visibility = View.VISIBLE
            } else if(target === "") {
                this.tipTV?.text = resources.getText(R.string.transaction_transfer_tips_target_error)
                this.tipTV?.visibility = View.VISIBLE
            } else if(amount <= 0) {
                this.tipTV?.text = resources.getText(R.string.transaction_transfer_tips_amount_error)
                this.tipTV?.visibility = View.VISIBLE
            } else {
                target = targetTV?.text.toString()
                amount = amountTV?.text.toString().toFloat()
                this.tipTV?.visibility = View.INVISIBLE
                it.visibility = View.GONE
                submitPB?.visibility = View.VISIBLE
                this.resolveVerify(address, target, chooseAssetId, amount)
            }

        }
    }

    private  fun initInput() {
        amountTV?.isEnabled = false
        amountTV?.setOnFocusChangeListener {_, hasFocus ->
            if(hasFocus) {
                balanceTV?.visibility = View.VISIBLE
            } else {
                balanceTV?.visibility = View.GONE
            }
        }
    }

    private fun send(from: String, to: String, assetId: String, amount: Float) {
        var asset: String = ""
        if (assetId.length === 64) {
            asset = "0x $assetId"
        }else if (assetId.length === 42) {
            asset = assetId.substring(2, assetId.length)
        } else {
            asset = assetId
        }
        Log.i("asset",asset)
        if(WalletUtils.check(assetId, "asset")) {
            HttpClient.post("getutxoes", listOf(from, asset), fun(res) {
                Log.i("UTXO", res.toString())
                val data = gson.fromJson<ArrayList<UtxoModel>>(res, object: TypeToken<ArrayList<UtxoModel>>() {}.type)
                Log.i("UTXO",data.toString())
                val newTx = TransactionModel.forAsset(data, from, to, amount, asset)
                newTx?.sign(wif)
                HttpClient.post("sendv4rawtransaction", listOf(newTx!!.serialize(true)), fun(res) {
                    Log.i("transaction",res.toString())
                }, fun(err) {

                })
            }, fun(err) {

            })
        } else  {

        }
    }

    private fun resolveVerify(from: String, to: String, assetId: String, amount: Float) {
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
                            send(from, to, assetId, amount)
                        }
                    }
                }
            }
        }
    }
}
