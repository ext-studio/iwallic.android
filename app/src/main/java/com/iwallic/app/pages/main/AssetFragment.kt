package com.iwallic.app.pages.main

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.adapters.AssetAdapter
import com.iwallic.app.base.BaseFragment
import com.iwallic.app.broadcasts.AssetBroadCast
import com.iwallic.app.broadcasts.BlockBroadCast
import com.iwallic.app.models.AssetRes
import com.iwallic.app.pages.asset.AssetDetailActivity
import com.iwallic.app.pages.asset.AssetManageActivity
import com.iwallic.app.states.AssetManageState
import com.iwallic.app.states.AssetState
import com.iwallic.app.utils.*
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader

class AssetFragment : BaseFragment() {
    private lateinit var address: String
    private lateinit var assetRV: RecyclerView
    private lateinit var assetSRL: SmartRefreshLayout
    private lateinit var refreshCH: ClassicsHeader
    private lateinit var assetAdapter: AssetAdapter
    private lateinit var assetManager: LinearLayoutManager
    private lateinit var mainAssetTV: TextView
    private lateinit var mainBalanceTV: TextView
    private lateinit var manageIV: ImageView

    private var broadCast: BlockBroadCast? = null
    private var assetBroadCast: AssetBroadCast? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_asset, container, false)
        initDOM(view)
        initListener()
        initList()
        initBroadCast()
        initAssetBroadCast()
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CommonUtils.requestBalanceUpdated) {
            initList()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        context?.unregisterReceiver(broadCast)
        context?.unregisterReceiver(assetBroadCast)
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

        address = SharedPrefUtils.getAddress(context)

        assetRV.isNestedScrollingEnabled = false
        assetRV.layoutManager = assetManager
        assetRV.adapter = assetAdapter
    }

    private fun initBroadCast() {
        broadCast = BlockBroadCast()
        broadCast?.setNewBlockListener { _, _ ->
            initList(true)
        }
        context?.registerReceiver(broadCast, IntentFilter(CommonUtils.broadCastBlock))
    }
    private fun initAssetBroadCast() {
        assetBroadCast = AssetBroadCast()
        assetBroadCast?.setOnAssetChangedListener { _, _ ->
            initList()
        }
        context?.registerReceiver(assetBroadCast, IntentFilter(CommonUtils.broadCastAsset))
    }

    private fun initListener() {
        assetSRL.setOnRefreshListener {
            initList(true)
        }
        manageIV.setOnClickListener {
            activity?.startActivity(Intent(context!!, AssetManageActivity::class.java))
        }
        assetAdapter.setOnAssetClickListener {
            val intent = Intent(context!!, AssetDetailActivity::class.java)
            intent.putExtra("asset", assetAdapter.getAssetId(it))
            activity?.startActivityForResult(intent, CommonUtils.requestBalanceUpdated)
        }
    }

    private fun initList(force: Boolean = false) {
        AssetState.list(context, address, force, {
            resolveList(it)
            assetSRL.finishRefresh(true)
        }, {
            assetSRL.finishRefresh()
            if (!DialogUtils.error(context, it)) {
                Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun resolveList(list: ArrayList<AssetRes>) {
        mainAssetTV.text = "NEO"

        val balance = list.find {
            it.asset_id == CommonUtils.NEO
        }?.balance ?: "0"

        mainBalanceTV.text = if (balance == "0.0") "0" else balance

        for (asset in AssetManageState.watch(context)) {
            if (list.indexOfFirst {
                it.asset_id == asset.asset_id
            } < 0) {
                list.add(asset)
            }
        }
        assetAdapter.set(list)
    }
}
