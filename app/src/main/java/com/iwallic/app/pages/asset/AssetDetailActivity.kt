package com.iwallic.app.pages.asset

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.iwallic.app.adapters.TransactionAdapter
import com.iwallic.app.broadcasts.BlockBroadCast
import com.iwallic.app.models.*
import com.iwallic.app.pages.common.BrowserActivity
import com.iwallic.app.pages.transaction.TransactionTransferActivity
import com.iwallic.app.states.AssetState
import com.iwallic.app.states.TransactionState
import com.iwallic.app.states.UnconfirmedState
import com.iwallic.app.utils.*
import com.scwang.smartrefresh.layout.SmartRefreshLayout

class AssetDetailActivity : BaseActivity() {
    private lateinit var titleTV: TextView
    private lateinit var balanceTV: TextView
    private lateinit var backIV: ImageView
    private lateinit var transferIV: ImageView
    private lateinit var asset: AssetRes
    private lateinit var txRV: RecyclerView
    private lateinit var txSRL: SmartRefreshLayout

    private lateinit var detailLL: LinearLayout
    private lateinit var claimLL: LinearLayout
    private lateinit var claimEnterTV: TextView
    private lateinit var claimCancelTV: TextView
    private lateinit var claimUnClaimTV: TextView
    private lateinit var claimUnCollectTV: TextView
    private lateinit var claimCollectB: Button
    private lateinit var claimClaimB: Button

    private var claims: ClaimsRes? = null
    private var broadCast: BlockBroadCast? = null

    private lateinit var txAdapter: TransactionAdapter
    private lateinit var txManager: LinearLayoutManager

    private val gson = Gson()
    private var noNeed = false
    private var assetId = ""
    private var changed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset_detail)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        initParams()
        initDOM()
//        initGAS()
        resolveFetchClaim()
        initListener()
        resolveBalance()
        initBroadCast()
        val loader = DialogUtils.loader(this)
        TransactionState.init(this, assetId, {
            loader.dismiss()
            txAdapter.set(it)
        }, {
            loader.dismiss()
            if (!DialogUtils.error(this, it)) {
                Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun finish() {
        if (changed) {
            setResult(Activity.RESULT_OK)
        }
        super.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadCast)
    }

    private fun initParams() {
        assetId = intent.getStringExtra("asset") ?: ""
        if (assetId.isEmpty()) {
            Toast.makeText(this, R.string.error_failed, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val tryGet = AssetState.get(assetId)
        if (tryGet == null) {
            Toast.makeText(this, R.string.error_failed, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        asset = tryGet
    }

    private fun initDOM() {
        titleTV = findViewById(R.id.asset_detail_title)
        backIV = findViewById(R.id.asset_detail_back)
        transferIV = findViewById(R.id.asset_detail_transfer)
        balanceTV = findViewById(R.id.asset_detail_balance)
        txRV = findViewById(R.id.asset_detail_list)
        txSRL = findViewById(R.id.asset_detail_pager)
        // loadPB = findViewById(R.id.asset_detail_load)

        detailLL = findViewById(R.id.asset_detail)
        claimLL = findViewById(R.id.asset_detail_claim)
        claimEnterTV = findViewById(R.id.asset_detail_claim_enter)
        claimCancelTV = findViewById(R.id.asset_detail_claim_cancel)
        claimUnCollectTV = findViewById(R.id.asset_detail_claim_un_collect)
        claimUnClaimTV = findViewById(R.id.asset_detail_claim_un_claim)
        claimClaimB = findViewById(R.id.asset_detail_claim_claim)
        claimCollectB = findViewById(R.id.asset_detail_claim_collect)

        setStatusBar(findViewById(R.id.app_top_space))

        // txSRL.setColorSchemeResources(R.color.colorPrimaryDefault)
        txAdapter = TransactionAdapter(arrayListOf())
        txManager = LinearLayoutManager(this)
        txRV.layoutManager = txManager
        txRV.adapter = txAdapter

        if (asset.balance.toDouble() <= 0) {
            transferIV.visibility = View.GONE
        }
    }

    private fun initBroadCast() {
        broadCast = BlockBroadCast()
        broadCast?.setNewBlockListener { _, _ ->
            AssetState.list(this, NeonUtils.address(this), true, {
                asset = it.find {a -> a.asset_id == assetId } ?: return@list
                titleTV.text = asset.symbol
                balanceTV.text = asset.balance
                changed = true
            }, {
                if (!DialogUtils.error(this, it)) {
                    Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                }
            })
            TransactionState.refresh(this, assetId, {
                txAdapter.set(it)
            }, {
                if (!DialogUtils.error(this, it)) {
                    Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                }
            })
        }
        registerReceiver(broadCast, IntentFilter(CommonUtils.broadCastBlock))
    }

    private fun initGAS() {
        if (asset.asset_id != CommonUtils.GAS) {
            return
        }
        claimEnterTV.setOnClickListener {
            detailLL.visibility = View.GONE
            claimLL.visibility = View.VISIBLE
        }
        claimCancelTV.setOnClickListener {
            detailLL.visibility = View.VISIBLE
            claimLL.visibility = View.GONE
        }
        claimClaimB.setOnClickListener {
            if (claims!!.unSpentClaim == "0") {
                Toast.makeText(this, R.string.error_claim_claim, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val lastCollect = SharedPrefUtils.getCollect(this)
            val lastClaim = SharedPrefUtils.getClaim(this)
            when {
                lastClaim.isNotEmpty() -> {
                    DialogUtils.confirm(this, null, R.string.error_claim_claiming, R.string.dialog_title_primary, R.string.dialog_ok)
                }
                lastCollect.isNotEmpty() -> {
                    DialogUtils.confirm(this, null, R.string.error_claim_collecting, R.string.dialog_title_primary, R.string.dialog_ok)
                }
                else -> {
                    resolveClaim()
                }
            }
        }
        claimCollectB.setOnClickListener {
            if (claims!!.unCollectClaim == "0") {
                Toast.makeText(this, R.string.error_claim_collect, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val lastCollect = SharedPrefUtils.getCollect(this)
            val lastClaim = SharedPrefUtils.getClaim(this)
            when {
                lastClaim.isNotEmpty() -> {
                    DialogUtils.confirm(this, null, R.string.error_claim_claiming, R.string.dialog_title_primary, R.string.dialog_ok)
                }
                lastCollect.isNotEmpty() -> {
                    DialogUtils.confirm(this, null, R.string.error_claim_collecting, R.string.dialog_title_primary, R.string.dialog_ok)
                }
                else -> {
                    resolveCollect()
                }
            }
        }
    }

    private fun initListener() {
        backIV.setOnClickListener {
            finish()
        }
        transferIV.setOnClickListener {
            val intent = Intent(this, TransactionTransferActivity::class.java)
            intent.putExtra("asset", asset.asset_id)
            startActivity(intent)
        }
        txAdapter.setOnTxEnterListener {
            val intent = Intent(this, BrowserActivity::class.java)
            intent.putExtra("url", "https://blolys.com/#/transaction/${txAdapter.getItem(it).txid}")
            startActivity(intent)
        }
        txAdapter.setOnTxCopyListener {
            copy(txAdapter.getItem(it).txid, "txid")
            vibrate()
        }
        txSRL.setOnRefreshListener { _ ->
            AssetState.list(this, NeonUtils.address(this), true, {
                asset = it.find {a -> a.asset_id == assetId } ?: return@list
                titleTV.text = asset.symbol
                balanceTV.text = asset.balance
                changed = true
            }, {
                if (!DialogUtils.error(this, it)) {
                    Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                }
            })
            TransactionState.refresh(this, assetId, {
                txSRL.finishRefresh(true)
                txAdapter.set(it)
            }, {
                txSRL.finishRefresh()
                if (!DialogUtils.error(this, it)) {
                    Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                }
            })
        }
        txSRL.setOnLoadMoreListener { _ ->
            TransactionState.older(this, assetId, {
                txAdapter.push(it)
                if (it.size > 0) {
                    txSRL.finishLoadMore(true)
                } else {
                    txSRL.finishLoadMoreWithNoMoreData()
                }
            }, {
                txSRL.finishLoadMore()
                if (!DialogUtils.error(this, it)) {
                    Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun resolveFetchClaim() {
        if (asset.asset_id != CommonUtils.GAS || noNeed) {
            return
        }
        HttpUtils.post(this,"getclaim", listOf(NeonUtils.address(this)), {
            claims = try {gson.fromJson(it, ClaimsRes::class.java)} catch(_: Throwable) {null}
            if (claims != null) {
                claimEnterTV.visibility = View.VISIBLE
                claimUnCollectTV.text = resources.getString(R.string.asset_detail_claim_un_collect, claims!!.unCollectClaim)
                claimUnClaimTV.text = resources.getString(R.string.asset_detail_claim_un_claimed, claims!!.unSpentClaim)
                if (claims!!.unCollectClaim == "0") {
                    claimCollectB.visibility = View.GONE
                }
                if (claims!!.unSpentClaim == "0") {
                    claimClaimB.visibility = View.GONE
                    if (!AssetState.checkClaim()) {
                        noNeed = true
                    }
                }
            }
        }, {
            Log.i("【GASDetail】", "check failed【$it】")
        })
    }

    private fun resolveBalance() {
        val tryGet = AssetState.get(asset.asset_id)
        titleTV.text = tryGet?.symbol
        balanceTV.text = tryGet?.balance
    }

    private fun resolveCollect() {
        val addr = NeonUtils.address(this)
        DialogUtils.password(this) {pwd ->
            val loader = DialogUtils.loader(this, R.string.asset_detail_collecting)
            NeonUtils.verify(this, pwd, { wif ->
                if (wif.isEmpty()) {
                    loader.dismiss()
                    resolveError(99599)
                } else {
                    NeonUtils.fetchBalance(this, addr, CommonUtils.GAS, { balance ->
                        var amount = 0.0
                        balance.forEach {
                            amount += it.value
                        }
                        val newTx = TransactionModel.forAsset(balance, addr, addr, amount, CommonUtils.GAS)
                        if (newTx == null) {
                            loader.dismiss()
                            resolveError(99699)
                        } else {
                            newTx.sign(wif)
                            HttpUtils.post(this, "sendv4rawtransaction", listOf(newTx.serialize(true)), {
                                loader.dismiss()
                                resolveSuccess(newTx.hash(), addr, amount, "collect")
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
            }, {
                loader.dismiss()
                resolveError(it)
            })
        }
    }

    private fun resolveClaim() {
        val addr = NeonUtils.address(this)
        DialogUtils.password(this) { pwd ->
            val loader = DialogUtils.loader(this, R.string.asset_detail_claiming)
            NeonUtils.verify(this, pwd, { wif ->
                if (wif.isEmpty()) {
                    loader.dismiss()
                    resolveError(99599)
                } else {
                    val newTx = TransactionModel.forClaim(claims!!.claims, claims!!.unSpentClaim.toDouble(), addr)
                    if (newTx == null) {
                        loader.dismiss()
                        resolveError(99699)
                    } else {
                        newTx.sign(wif)
                        HttpUtils.post(this, "sendv4rawtransaction", listOf(newTx.serialize(true)), {
                            loader.dismiss()
                            resolveSuccess(newTx.hash(), addr, claims!!.unSpentClaim.toDouble(), "claim")
                        }, {
                            loader.dismiss()
                            resolveError(it)
                        })
                    }
                }
            }, {
                loader.dismiss()
                resolveError(it)
            })
        }
    }

    private fun resolveSuccess(txid: String, addr: String, value: Double, type: String) {
        when (type) {
            "claim" -> {
                SharedPrefUtils.setClaim(this, txid)
            }
            "collect" -> {
                SharedPrefUtils.setCollect(this, txid)
            }
        }
        HttpUtils.postPy(
            this,
            "/client/transaction/unconfirmed",
            mapOf(Pair("wallet_address", addr), Pair("asset_id", CommonUtils.GAS), Pair("txid", "0x$txid"), Pair("value", "$value")
        ), {
                Log.i("【Claim】", "submitted【$txid】")
                UnconfirmedState.clear(this)
            }, {
                Log.i("【Claim】", "submit failed【$it】")
            }
        )
        Toast.makeText(this, if (type == "claim") R.string.asset_detail_claim_success else R.string.asset_detail_collect_success, Toast.LENGTH_SHORT).show()
        detailLL.visibility = View.VISIBLE
        claimLL.visibility = View.GONE
    }

    private fun resolveError(code: Int) {
        if (!DialogUtils.error(this, code)) {
            Toast.makeText(this, "$code", Toast.LENGTH_SHORT).show()
        }
    }
}
