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
import com.iwallic.app.models.TransactionModel
import com.iwallic.app.states.AssetState
import com.iwallic.app.utils.HttpClient
import com.iwallic.app.utils.WalletUtils
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
            it.visibility = View.GONE
            submitPB?.visibility = View.VISIBLE
            target = targetTV?.text.toString()
            amount = amountTV?.text.toString().toFloat()
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

}
