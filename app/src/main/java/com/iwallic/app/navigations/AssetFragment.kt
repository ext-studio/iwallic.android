package com.iwallic.app.navigations

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.adapters.AssetAdapter
import com.iwallic.app.base.BaseFragment
import com.iwallic.app.models.AssetRes
import com.iwallic.app.pages.asset.AssetDetailActivity
import com.iwallic.app.pages.asset.AssetManageActivity
import com.iwallic.app.services.new_block_action
import com.iwallic.app.states.AssetState
import com.iwallic.app.utils.*
import io.reactivex.disposables.Disposable

class AssetFragment : BaseFragment() {
    private lateinit var assetRV: RecyclerView
    private lateinit var assetSRL: SwipeRefreshLayout
    private lateinit var assetAdapter: AssetAdapter
    private lateinit var assetManager: RecyclerView.LayoutManager
    private lateinit var mainAssetTV: TextView
    private lateinit var mainBalanceTV: TextView
    private lateinit var loadPB: ProgressBar
    private lateinit var manageIV: ImageView
    private val mainAsset = AssetRes("0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "0", "NEO", "NEO")

    private lateinit var listListen: Disposable
    private lateinit var errorListen: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_asset, container, false)
        initDOM(view)
        initListener()
        context!!.registerReceiver(BlockListener, IntentFilter(new_block_action))
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listListen.dispose()
        errorListen.dispose()
        context!!.unregisterReceiver(BlockListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initDOM(view: View) {
        assetRV = view.findViewById(R.id.asset_list)
        assetSRL = view.findViewById(R.id.asset_list_refresh)
        assetSRL.setColorSchemeResources(R.color.colorPrimaryDefault)
        mainAssetTV = view.findViewById(R.id.fragment_asset_main_asset)
        mainBalanceTV = view.findViewById(R.id.fragment_asset_main_balance)
        loadPB = view.findViewById(R.id.fragment_asset_load)
        manageIV = view.findViewById(R.id.fragment_asset_manage)
        assetAdapter = AssetAdapter(arrayListOf())
        assetManager = LinearLayoutManager(context!!)
        assetRV.layoutManager = assetManager
        assetRV.adapter = assetAdapter
    }

    private fun initListener() {
        listListen = AssetState.list(WalletUtils.address(context!!)).subscribe({
            resolveList(it)
            resolveRefreshed(true)
        }, {
            resolveRefreshed()
            Log.i("【AssetList】", "error【${it}】")
        })
        errorListen = AssetState.error().subscribe({
            resolveRefreshed()
            if (!DialogUtils.error(context!!, it)) {
                Toast.makeText(context!!, it.toString(), Toast.LENGTH_SHORT).show()
            }
        }, {
            resolveRefreshed()
            Log.i("【AssetList】", "error【${it}】")
        })
        assetSRL.setOnRefreshListener {
            if (AssetState.fetching) {
                assetSRL.isRefreshing = false
                return@setOnRefreshListener
            }
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
        mainBalanceTV.text = list.find {
            it.asset_id == mainAsset.asset_id
        }?.balance
        for (asset in SharedPrefUtils.getAsset(context!!)) {
            if (list.indexOfFirst {
                it.asset_id == asset.asset_id
            } < 0) {
                list.add(asset)
            }
        }
        assetAdapter.set(list)
    }

    private fun resolveRefreshed(success: Boolean = false) {
        if (loadPB.visibility == View.VISIBLE) {
            loadPB.visibility = View.GONE
        }
        if (!assetSRL.isRefreshing) {
            return
        }
        assetSRL.isRefreshing = false
        if (success) {
            Toast.makeText(context!!, R.string.toast_refreshed, Toast.LENGTH_SHORT).show()
        }
    }

    companion object BlockListener: BroadcastReceiver() {
        val TAG: String = AssetFragment::class.java.simpleName
        fun newInstance() = AssetFragment()

        override fun onReceive(p0: Context?, p1: Intent?) {
            AssetState.fetch("", true)
        }
    }
}
