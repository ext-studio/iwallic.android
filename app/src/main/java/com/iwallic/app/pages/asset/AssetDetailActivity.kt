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
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.iwallic.app.adapters.TransactionAdapter
import com.iwallic.app.models.*
import com.iwallic.app.pages.transaction.TransactionDetailActivity
import com.iwallic.app.pages.transaction.TransactionTransferActivity
import com.iwallic.app.services.new_block_action
import com.iwallic.app.states.AssetState
import com.iwallic.app.states.TransactionState
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.GAS
import com.iwallic.app.utils.HttpUtils
import com.iwallic.app.utils.WalletUtils
import io.reactivex.disposables.Disposable

class AssetDetailActivity : BaseActivity() {
    private lateinit var titleTV: TextView
    private lateinit var balanceTV: TextView
    private lateinit var backIV: ImageView
    private lateinit var transferIV: ImageView
    private lateinit var asset: AssetRes
    private lateinit var loadPB: ProgressBar
    private lateinit var txRV: RecyclerView
    private lateinit var txSRL: SwipeRefreshLayout

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset_detail)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        initParams()
        initDOM()
        initGAS()
        initListener()
        resolveBalance()
        registerReceiver(BlockListener, IntentFilter(new_block_action))
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
        txSRL = findViewById(R.id.asset_detail_list_refresh)
        loadPB = findViewById(R.id.asset_detail_load)

        detailLL = findViewById(R.id.asset_detail)
        claimLL = findViewById(R.id.asset_detail_claim)
        claimEnterTV = findViewById(R.id.asset_detail_claim_enter)
        claimCancelTV = findViewById(R.id.asset_detail_claim_cancel)
        claimUnCollectTV = findViewById(R.id.asset_detail_claim_un_collect)
        claimUnClaimTV = findViewById(R.id.asset_detail_claim_un_claim)
        claimClaimB = findViewById(R.id.asset_detail_claim_claim)
        claimCollectB = findViewById(R.id.asset_detail_claim_collect)

        txSRL.setColorSchemeResources(R.color.colorPrimaryDefault)
        txAdapter = TransactionAdapter(PageDataPyModel())
        txManager = LinearLayoutManager(this)
        txRV.layoutManager = txManager
        txRV.adapter = txAdapter
    }

    private fun initGAS() {
        if (asset.asset_id != GAS || !AssetState.checkClaim()) {
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
                }
            }
        }, {
            Log.i("【GASDetail】", "check failed【$it】")
        })
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
            Toast.makeText(this, R.string.error_incoming, Toast.LENGTH_SHORT).show()
        }
        claimCollectB.setOnClickListener {
            if (claims!!.unCollectClaim == "0") {
                Toast.makeText(this, R.string.error_claim_collect, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, R.string.error_incoming, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initListener() {
        listListen = TransactionState.list(WalletUtils.address(this), asset.asset_id).subscribe({
            resolveList(it)
            resolveRefreshed(true)
        }, {
            resolveRefreshed()
            Log.i("【AssetDetail】", "error【${it}】")
        })
        errorListen = TransactionState.error().subscribe({
            resolveRefreshed()
            if (!DialogUtils.error(this, it)) {
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }
        }, {
            resolveRefreshed()
            Log.i("【AssetDetail】", "error【${it}】")
        })
        balanceListen = AssetState.list(WalletUtils.address(this)).subscribe({
            resolveBalance()
        }, {
            Log.i("【AssetDetail】", "error【${it}】")
        })
        txSRL.setOnRefreshListener {
            if (TransactionState.fetching) {
                txSRL.isRefreshing = false
                return@setOnRefreshListener
            }
            TransactionState.fetch()
        }
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
        txRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!TransactionState.fetching && newState == 1 && txAdapter.checkNext(txManager.findLastVisibleItemPosition())) {
                    txAdapter.setPaging()
                    TransactionState.next()
                }
            }
        })
        txAdapter.onEnter().subscribe {
            val intent = Intent(this, TransactionDetailActivity::class.java)
            intent.putExtra("txid", txAdapter.getItem(it).txid)
            startActivity(intent)
        }
        txAdapter.onCopy().subscribe {
            copy(txAdapter.getItem(it).txid, "txid")
            vibrate()
        }
    }

    private fun resolveBalance() {
        val tryGet = AssetState.get(asset.asset_id)
        titleTV.text = tryGet?.symbol
        balanceTV.text = tryGet?.balance
    }
    private fun resolveList(data: PageDataPyModel<TransactionRes>) {
        txAdapter.push(data)
        resolveRefreshed(true)
    }

    private fun resolveRefreshed(success: Boolean = false) {
        if (loadPB.visibility == View.VISIBLE) {
            loadPB.visibility = View.GONE
        }
        if (!txSRL.isRefreshing) {
            return
        }
        txSRL.isRefreshing = false
        if (success) {
            Toast.makeText(this, R.string.toast_refreshed, Toast.LENGTH_SHORT).show()
        }
    }

    companion object BlockListener: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            AssetState.fetch("", silent = true)
            // TransactionState.fetch("", silent = true)
        }
    }
}
