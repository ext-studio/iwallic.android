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
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.iwallic.app.adapters.TransactionAdapter
import com.iwallic.app.models.PageDataRes
import com.iwallic.app.models.BalanceRes
import com.iwallic.app.models.TransactionRes
import com.iwallic.app.pages.transaction.TransactionDetailActivity
import com.iwallic.app.services.new_block_action
import com.iwallic.app.states.AssetState
import com.iwallic.app.states.TransactionState
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.WalletUtils
import io.reactivex.disposables.Disposable

class AssetDetailActivity : BaseActivity() {
    private lateinit var titleTV: TextView
    private lateinit var balanceTV: TextView
    private lateinit var backIV: ImageView
    private lateinit var asset: BalanceRes
    private lateinit var loadPB: ProgressBar
    private lateinit var txRV: RecyclerView
    private lateinit var txSRL: SwipeRefreshLayout

    private lateinit var txAdapter: TransactionAdapter
    private lateinit var txManager: LinearLayoutManager

    private lateinit var balanceListen: Disposable
    private lateinit var listListen: Disposable
    private lateinit var errorListen: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset_detail)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        initParams()
        initDOM()
        initListener()
        resolveBalance()
        // resolveList(PageDataRes())
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
        titleTV = findViewById(R.id.asset_detail)
        backIV = findViewById(R.id.asset_detail_back)
        balanceTV = findViewById(R.id.asset_detail_balance)
        txRV = findViewById(R.id.asset_detail_list)
        txSRL = findViewById(R.id.asset_detail_list_refresh)
        loadPB = findViewById(R.id.asset_detail_load)
        txSRL.setColorSchemeResources(R.color.colorPrimaryDefault)
        txAdapter = TransactionAdapter(PageDataRes())
        txManager = LinearLayoutManager(this)
        txRV.layoutManager = txManager
        txRV.adapter = txAdapter
    }

    private fun initListener() {
        listListen = TransactionState.list(WalletUtils.address(this), asset.assetId).subscribe({
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
    }

    private fun resolveBalance() {
        val tryGet = AssetState.get(asset.assetId)
        titleTV.text = tryGet?.symbol
        balanceTV.text = tryGet?.balance
    }
    private fun resolveList(data: PageDataRes<TransactionRes>) {
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
