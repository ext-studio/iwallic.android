package com.iwallic.app.navigations

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
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import com.iwallic.app.R
import com.iwallic.app.adapters.AssetAdapter
import com.iwallic.app.models.addrassets
import com.iwallic.app.pages.asset.AssetDetailActivity
import com.iwallic.app.pages.user.UserSettingActivity
import com.iwallic.app.services.new_block_action
import com.iwallic.app.states.AssetState
import com.iwallic.app.utils.RecyclerItemClickListener
import com.iwallic.app.utils.WalletUtils
import com.iwallic.app.utils.affectOnItemClick
import io.reactivex.disposables.Disposable

class AssetFragment : Fragment() {
    private lateinit var assetRV: RecyclerView
    private lateinit var assetSRL: SwipeRefreshLayout
    private lateinit var assetAdapter: RecyclerView.Adapter<*>
    private lateinit var assetManager: RecyclerView.LayoutManager
    private lateinit var mainAssetTV: TextView
    private lateinit var mainBalanceTV: TextView
    private val mainAsset: String = "NEO"

    private lateinit var listListen: Disposable
    private lateinit var errorListen: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_asset, container, false)

        assetRV = view.findViewById(R.id.asset_list)
        assetSRL = view.findViewById(R.id.asset_list_refresh)
        assetSRL.setColorSchemeResources(R.color.colorPrimaryDefault)
        mainAssetTV = view.findViewById(R.id.fragment_asset_main_asset)
        mainBalanceTV = view.findViewById(R.id.fragment_asset_main_balance)

        resolveList(arrayListOf())

        listListen = AssetState.list(WalletUtils.address(context!!)).subscribe({
            resolveList(it)
            resolveRefreshed(true)
        }, {
            resolveRefreshed()
            Log.i("资产列表", "发生错误【${it}】")
        })
        errorListen = AssetState.error().subscribe({
            resolveRefreshed()
            Toast.makeText(context!!, it.toString(), Toast.LENGTH_SHORT).show()
        }, {
            resolveRefreshed()
            Log.i("资产列表", "发生错误【${it}】")
        })

        assetSRL.setOnRefreshListener {
            AssetState.fetch()
        }

        context!!.registerReceiver(BlockListener, IntentFilter(new_block_action))
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listListen.dispose()
        errorListen.dispose()
        context!!.unregisterReceiver(BlockListener)
    }

    private fun resolveList(list: ArrayList<addrassets>) {
        mainAssetTV.text = "NEO"
        mainBalanceTV.text = list.find {
            it.symbol == "NEO"
        }?.balance
        assetManager = LinearLayoutManager(context!!)
        assetAdapter = AssetAdapter(list)
        assetRV.apply {
            setHasFixedSize(true)
            layoutManager = assetManager
            adapter = assetAdapter
        }
    }

    private fun resolveRefreshed(success: Boolean = true) {
        if (!assetSRL.isRefreshing) {
            return
        }
        assetSRL.isRefreshing = false
        if (success) {
            Toast.makeText(context!!, "数据已更新", Toast.LENGTH_SHORT).show()
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
