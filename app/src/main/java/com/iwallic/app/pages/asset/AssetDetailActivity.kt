package com.iwallic.app.pages.asset

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
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
import com.iwallic.app.models.*
import com.iwallic.app.pages.transaction.TransactionDetailActivity
import com.iwallic.app.pages.transaction.TransactionTransferActivity
import com.iwallic.app.states.AssetState
import com.iwallic.app.states.TransactionState
import com.iwallic.app.states.UnconfirmedState
import com.iwallic.app.utils.*
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import io.reactivex.disposables.Disposable

class AssetDetailActivity : BaseActivity() {
    private lateinit var titleTV: TextView
    private lateinit var balanceTV: TextView
    private lateinit var backIV: ImageView
    private lateinit var transferIV: ImageView
    private lateinit var asset: AssetRes
    // private lateinit var loadPB: ProgressBar
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

    private lateinit var txAdapter: TransactionAdapter
    private lateinit var txManager: LinearLayoutManager

    private lateinit var balanceListen: Disposable
    private lateinit var listListen: Disposable
    private lateinit var errorListen: Disposable
    private val gson = Gson()
    private var noNeed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset_detail)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        initParams()
        initDOM()
        initGAS()
        initListener()
        resolveBalance()
        registerReceiver(BlockListener, IntentFilter(CommonUtils.ACTION_NEWBLOCK))
    }

    override fun onDestroy() {
        super.onDestroy()
        listListen.dispose()
        balanceListen.dispose()
        errorListen.dispose()
        unregisterReceiver(BlockListener)
    }

    private fun initParams() {
        val assetId = intent.getStringExtra("asset")
        if (assetId.isNullOrEmpty()) {
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
        txAdapter = TransactionAdapter(PageDataPyModel())
        txManager = LinearLayoutManager(this)
        txRV.layoutManager = txManager
        txRV.adapter = txAdapter

        txSRL.setEnableOverScrollDrag(true)
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
                    DialogUtils.confirm(this, R.string.dialog_title_primary, R.string.error_claim_claiming, R.string.dialog_ok).subscribe()
                }
                lastCollect.isNotEmpty() -> {
                    DialogUtils.confirm(this, R.string.dialog_title_primary, R.string.error_claim_collecting, R.string.dialog_ok).subscribe()
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
                    DialogUtils.confirm(this, R.string.dialog_title_primary, R.string.error_claim_claiming, R.string.dialog_ok).subscribe()
                }
                lastCollect.isNotEmpty() -> {
                    DialogUtils.confirm(this, R.string.dialog_title_primary, R.string.error_claim_collecting, R.string.dialog_ok).subscribe()
                }
                else -> {
                    resolveCollect()
                }
            }
        }
    }

    private fun initListener() {
        listListen = TransactionState.list(WalletUtils.address(this), asset.asset_id).subscribe({
            txAdapter.push(it)
            txSRL.finishRefresh()
            if (it.page >= it.pages) {
                txSRL.finishLoadMoreWithNoMoreData()
            } else {
                txSRL.finishLoadMore(true)
            }
        }, {
            txSRL.finishRefresh()
            txSRL.finishLoadMore()
            Log.i("【AssetDetail】", "error【${it}】")
        })
        errorListen = TransactionState.error().subscribe({
            txSRL.finishRefresh()
            txSRL.finishLoadMore()
            if (!DialogUtils.error(this, it)) {
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }
        }, {
            txSRL.finishRefresh()
            txSRL.finishLoadMore()
            Log.i("【AssetDetail】", "error【${it}】")
        })
        balanceListen = AssetState.list(WalletUtils.address(this)).subscribe({
            resolveBalance()
            resolveFetchClaim()
        }, {
            Log.i("【AssetDetail】", "error【${it}】")
        })
//        txSRL.setOnRefreshListener {
//            if (TransactionState.fetching) {
//                txSRL.isRefreshing = false
//                return@setOnRefreshListener
//            }
//            TransactionState.fetch()
//        }
        backIV.setOnClickListener {
            finish()
        }
        if (asset.balance.toDouble() <= 0) {
            transferIV.visibility = View.GONE
        }
        transferIV.setOnClickListener {
            val intent = Intent(this, TransactionTransferActivity::class.java)
            intent.putExtra("asset", asset.asset_id)
            startActivity(intent)
        }
//        txRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (!TransactionState.fetching && newState == 1 && txAdapter.checkNext(txManager.findLastVisibleItemPosition())) {
//                    txAdapter.setPaging()
//                    TransactionState.next()
//                }
//            }
//        })
        txAdapter.onEnter().subscribe {
            val intent = Intent(this, TransactionDetailActivity::class.java)
            intent.putExtra("txid", txAdapter.getItem(it).txid)
            startActivity(intent)
        }
        txAdapter.onCopy().subscribe {
            copy(txAdapter.getItem(it).txid, "txid")
            vibrate()
        }
        txSRL.setOnRefreshListener {
            TransactionState.fetch()
        }
        txSRL.setOnLoadMoreListener {
            TransactionState.next()
        }
    }

    private fun resolveFetchClaim() {
        if (asset.asset_id != CommonUtils.GAS || noNeed) {
            return
        }
        HttpUtils.post("getclaim", listOf(WalletUtils.address(this)), {
            claims = gson.fromJson(it, ClaimsRes::class.java)
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

//    private fun resolveRefreshed(success: Boolean = false) {
//        if (loadPB.visibility == View.VISIBLE) {
//            loadPB.visibility = View.GONE
//        }
//        if (!txSRL.isRefreshing) {
//            return
//        }
//        txSRL.isRefreshing = false
//        if (success) {
//            Toast.makeText(this, R.string.toast_refreshed, Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun resolveCollect() {
        val addr = WalletUtils.address(this)
        DialogUtils.password(this).subscribe {pwd ->
            DialogUtils.load(this).subscribe {load ->
                WalletUtils.verify(this, pwd).subscribe({wif ->
                    if (wif.isEmpty()) {
                        load.dismiss()
                        resolveError(99599)
                    } else {
                        HttpUtils.post("getutxoes", listOf(addr, CommonUtils.GAS), {res ->
                            val data = gson.fromJson<ArrayList<UtxoModel>>(res, object: TypeToken<ArrayList<UtxoModel>>() {}.type)
                            if (data == null) {
                                load.dismiss()
                                resolveError(99998)
                                return@post
                            }
                            var amount = 0.0
                            data.forEach {
                                amount += it.value
                            }
                            val newTx = TransactionModel.forAsset(data, addr, addr, amount, CommonUtils.GAS)
                            if (newTx == null) {
                                load.dismiss()
                                resolveError(99699)
                                return@post
                            }
                            newTx.sign(wif)
                            Log.i("【Claim】", newTx.serialize(true))
//                            resolveSuccess(newTx.hash(), addr, amount, "collect")
                            HttpUtils.post("sendv4rawtransaction", listOf(newTx.serialize(true)), {
                                load.dismiss()
                                resolveSuccess(newTx.hash(), addr, amount, "collect")
                            }, {
                                load.dismiss()
                                resolveError(it)
                            })
                        }, {
                            load.dismiss()
                            resolveError(it)
                        })
                    }
                }, {
                    load.dismiss()
                    resolveError(99999)
                })
            }
        }
    }

    private fun resolveClaim() {
        val addr = WalletUtils.address(this)
        DialogUtils.password(this).subscribe {pwd ->
            DialogUtils.load(this).subscribe {load ->
                WalletUtils.verify(this, pwd).subscribe({wif ->
                    if (wif.isEmpty()) {
                        load.dismiss()
                        resolveError(99599)
                    } else {
                        val newTx = TransactionModel.forClaim(claims!!.claims, claims!!.unSpentClaim.toDouble(), addr)
                        if (newTx == null) {
                            load.dismiss()
                            resolveError(99699)
                        } else {
                            newTx.sign(wif)
                            Log.i("【Claim】", newTx.serialize(true))
//                            resolveSuccess(newTx.hash(), addr, claims!!.unSpentClaim.toDouble(), "claim")
                            HttpUtils.post("sendv4rawtransaction", listOf(newTx.serialize(true)), {
                                load.dismiss()
                                resolveSuccess(newTx.hash(), addr, claims!!.unSpentClaim.toDouble(), "claim")
                            }, {
                                load.dismiss()
                                resolveError(it)
                            })
                        }
                    }
                }, {
                    load.dismiss()
                    resolveError(99999)
                })
            }
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
            "/client/transaction/unconfirmed",
            mapOf(Pair("wallet_address", addr), Pair("asset_id", CommonUtils.GAS), Pair("txid", "0x$txid"), Pair("value", "$value")), {
                Log.i("【Claim】", "submitted 【$txid】")
                UnconfirmedState.fetch()
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

    companion object BlockListener: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            AssetState.fetch("", silent = true)
        }
    }
}
