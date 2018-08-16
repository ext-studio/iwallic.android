package com.iwallic.app.pages.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.adapters.AssetAdapter
import com.iwallic.app.base.BaseFragment
import com.iwallic.app.models.AssetRes
import com.iwallic.app.pages.asset.AssetDetailActivity
import com.iwallic.app.pages.asset.AssetManageActivity
import com.iwallic.app.states.AssetState
import com.iwallic.app.utils.*
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import io.reactivex.disposables.Disposable

class AssetFragment : BaseFragment() {
    private lateinit var assetRV: RecyclerView
    private lateinit var assetSRL: SmartRefreshLayout
    private lateinit var refreshCH: ClassicsHeader
    private lateinit var assetAdapter: AssetAdapter
    private lateinit var assetManager: RecyclerView.LayoutManager
    private lateinit var mainAssetTV: TextView
    private lateinit var mainBalanceTV: TextView
    private lateinit var manageIV: ImageView
    private val mainAsset = AssetRes("0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "0", "NEO", "NEO")

    private lateinit var listListen: Disposable
    private lateinit var errorListen: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_asset, container, false)
        initDOM(view)
        initListener()
        context?.registerReceiver(BlockListener, IntentFilter(CommonUtils.ACTION_NEWBLOCK))
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listListen.dispose()
        errorListen.dispose()
        context!!.unregisterReceiver(BlockListener)
    }

    private fun initDOM(view: View) {
        assetRV = view.findViewById(R.id.asset_list)
        assetSRL = view.findViewById(R.id.asset_list_pager)
        refreshCH = view.findViewById(R.id.asset_list_refresh)
        mainAssetTV = view.findViewById(R.id.fragment_asset_main_asset)
        mainBalanceTV = view.findViewById(R.id.fragment_asset_main_balance)
        manageIV = view.findViewById(R.id.fragment_asset_manage)
        assetAdapter = AssetAdapter(arrayListOf())
        assetManager = LinearLayoutManager(context!!)
        assetRV.layoutManager = assetManager
        assetRV.adapter = assetAdapter
    }

    private fun initListener() {
        listListen = AssetState.list(WalletUtils.address(context!!)).subscribe({
            resolveList(it)
            assetSRL.finishRefresh(true)
        }, {
            assetSRL.finishRefresh(false)
            Log.i("【AssetList】", "error【${it}】")
        })
        errorListen = AssetState.error().subscribe({
            assetSRL.finishRefresh(false)
            if (!DialogUtils.error(context!!, it)) {
                Toast.makeText(context!!, it.toString(), Toast.LENGTH_SHORT).show()
            }
        }, {
            assetSRL.finishRefresh(false)
            Log.i("【AssetList】", "error【${it}】")
        })
        assetSRL.setOnRefreshListener {
            AssetState.fetch()
        }
        manageIV.setOnClickListener {
            activity!!.startActivity(Intent(context!!, AssetManageActivity::class.java))
        }
        assetAdapter.onClick().subscribe {
            val intent = Intent(context!!, AssetDetailActivity::class.java)
            intent.putExtra("asset", assetAdapter.getAssetId(it))
            context!!.startActivity(intent)
        }
    }

    private fun resolveList(list: ArrayList<AssetRes>) {
        mainAssetTV.text = mainAsset.name

        val balance = list.find {
            it.asset_id == mainAsset.asset_id
        }?.balance ?: "0"

        mainBalanceTV.text = if (balance == "0.0") "0" else balance

        for (asset in SharedPrefUtils.getAsset(context!!)) {
            if (list.indexOfFirst {
                it.asset_id == asset.asset_id
            } < 0) {
                list.add(asset)
            }
        }
        assetAdapter.set(list)
    }

    companion object BlockListener: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            AssetState.fetch("", true)
        }
    }
}
