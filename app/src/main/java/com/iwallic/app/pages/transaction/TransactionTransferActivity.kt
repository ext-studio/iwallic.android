package com.iwallic.app.pages.transaction

import android.content.Context
import android.os.Bundle
import android.renderscript.Sampler
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.iwallic.app.utils.DialogUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.iwallic.app.states.AssetState
import com.iwallic.app.utils.HttpClient
import com.iwallic.app.utils.WalletUtils

class TransactionTransferActivity : BaseActivity() {

    private var backL: LinearLayout ?= null
    private var chooseAssetR: RelativeLayout ?= null
    private var targetTV: TextView ?= null
    private var amountTV: TextView ?= null
    private var assetNameTV: TextView ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_transfer)
        this.initDOM()
        this.initClick()
    }

    private fun initDOM() {
        backL = findViewById(R.id.transaction_transfer_back)
        chooseAssetR = findViewById(R.id.transaction_transfer_choose_asset)
        targetTV = findViewById(R.id.transaction_transfer_target)
        amountTV = findViewById(R.id.transaction_transfer_amount)
        assetNameTV = findViewById(R.id.asset_choose_name)
    }

    private fun initClick() {
        backL!!.setOnClickListener {
            this.finish()
        }
        chooseAssetR!!.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
            val list = mapOf(Pair("0xneo","NEO"), Pair( "0xgas","GAS"), Pair("0xext","EXT"))
            DialogUtils.DialogList(this, R.string.dialog_title_choose, list, fun (confirm: String) {
                assetNameTV!!.text = list[confirm]
            })
        }
    }

}
