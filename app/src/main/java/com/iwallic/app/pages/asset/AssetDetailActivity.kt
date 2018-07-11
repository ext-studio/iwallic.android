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
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.iwallic.app.adapters.TransactionAdapter
import com.iwallic.app.models.addrassets
import com.iwallic.app.models.transactions
import com.iwallic.app.services.new_block_action
import com.iwallic.app.states.AssetState
import com.iwallic.app.states.TransactionState
import com.iwallic.app.utils.WalletUtils
import io.reactivex.disposables.Disposable

class AssetDetailActivity : BaseActivity() {
    private lateinit var titleTV: TextView
    private lateinit var balanceTV: TextView
    private lateinit var backIV: ImageView
    private lateinit var asset: addrassets

    private lateinit var txRV: RecyclerView
    private lateinit var txSRL: SwipeRefreshLayout
    private lateinit var txAdapter: RecyclerView.Adapter<*>
    private lateinit var txManager: RecyclerView.LayoutManager

    private lateinit var balanceListen: Disposable
    private lateinit var listListen: Disposable
    private lateinit var errorListen: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset_detail)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        initParams()

        titleTV = findViewById(R.id.asset_detail)
        backIV = findViewById(R.id.asset_detail_back)
        balanceTV = findViewById(R.id.asset_detail_balance)
        txRV = findViewById(R.id.asset_detail_list)
        txSRL = findViewById(R.id.asset_detail_list_refresh)
        txSRL.setColorSchemeResources(R.color.colorPrimaryDefault)

        resolveBalance()
        resolveList(arrayListOf())

        listListen = TransactionState.list(WalletUtils.address(this), asset.assetId).subscribe({
            resolveList(it.data)
            resolveRefreshed(true)
        }, {
            resolveRefreshed()
            Log.i("资产详情", "发生错误【${it}】")
        })
        errorListen = TransactionState.error().subscribe({
            resolveRefreshed()
            Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
        }, {
            resolveRefreshed()
            Log.i("资产详情", "发生错误【${it}】")
        })
        balanceListen = AssetState.list(WalletUtils.address(this)).subscribe({}, {})
        txSRL.setOnRefreshListener {
            TransactionState.fetch()
        }
        backIV.setOnClickListener {
            finish()
        }

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
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val tryGet = AssetState.get(assetId)
        if (tryGet == null) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        asset = tryGet
    }
    private fun resolveBalance() {
        val tryGet = AssetState.get(asset.assetId)
        titleTV.text = tryGet?.symbol
        balanceTV.text = tryGet?.balance
    }
    private fun resolveList(list: ArrayList<transactions>) {
        txManager = LinearLayoutManager(this)
        txAdapter = TransactionAdapter(list)
        txRV.apply {
            setHasFixedSize(true)
            layoutManager = txManager
            adapter = txAdapter
        }
    }

    private fun resolveRefreshed(success: Boolean = true) {
        if (!txSRL.isRefreshing) {
            return
        }
        txSRL.isRefreshing = false
        if (success) {
            Toast.makeText(this, "数据已更新", Toast.LENGTH_SHORT).show()
        }
    }

    companion object BlockListener: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            AssetState.fetch("", silent = true)
            TransactionState.fetch("", silent = true)
        }
    }
}
