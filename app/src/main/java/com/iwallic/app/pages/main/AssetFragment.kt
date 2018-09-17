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
    private lateinit var address: String
    private lateinit var assetRV: RecyclerView
    private lateinit var assetSRL: SmartRefreshLayout
    private lateinit var refreshCH: ClassicsHeader
    private lateinit var assetAdapter: AssetAdapter
    private lateinit var assetManager: RecyclerView.LayoutManager
    private lateinit var mainAssetTV: TextView
    private lateinit var mainBalanceTV: TextView
    private lateinit var manageIV: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_asset, container, false)
        initDOM(view)
        initListener()
        initList()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
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

    private fun initListener() {
        assetSRL.setOnRefreshListener {
            initList(true)
        }
        manageIV.setOnClickListener {
            context?.startActivity(Intent(context!!, AssetManageActivity::class.java))
        }
        assetAdapter.onClick().subscribe {
            val intent = Intent(context!!, AssetDetailActivity::class.java)
            intent.putExtra("asset", assetAdapter.getAssetId(it))
            context?.startActivity(intent)
        }
    }

    private fun initList(force: Boolean = false) {
        AssetState.list2(context, address, force).subscribe({
            resolveList(it)
        }, {
            val code = try {it.message?.toInt()?:99999}catch (_: Throwable){99999}
            if (!DialogUtils.error(context, code)) {
                Toast.makeText(context, "$code", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun resolveList(list: ArrayList<AssetRes>) {
        mainAssetTV.text = "NEO"

        val balance = list.find {
            it.asset_id == CommonUtils.NEO
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
}
