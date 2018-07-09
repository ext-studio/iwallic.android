package com.iwallic.app.navigations

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast

import com.iwallic.app.R
import com.iwallic.app.adapters.AssetAdapter
import com.iwallic.app.models.addrassets
import com.iwallic.app.services.new_block_action
import com.iwallic.app.states.AssetState
import com.iwallic.app.utils.WalletUtils
import io.reactivex.functions.Consumer

class AssetFragment : Fragment() {
    private var assetLV: ListView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_asset, container, false)
        assetLV = view.findViewById(R.id.asset_list)

        AssetState.list(WalletUtils.address(context!!)).subscribe(Consumer {
            resolveList(it)
        })
        AssetState.error().subscribe(Consumer {
            Toast.makeText(context!!, it.toString(), Toast.LENGTH_SHORT).show()
        })

        context!!.registerReceiver(BlockListener, IntentFilter(new_block_action))
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        context!!.unregisterReceiver(BlockListener)
    }

    private fun resolveList(list: ArrayList<addrassets>) {
        val adapter = AssetAdapter(context!!, R.layout.adapter_asset_list, list)
        assetLV!!.adapter = adapter
    }

    companion object BlockListener: BroadcastReceiver() {
        val TAG: String = AssetFragment::class.java.simpleName
        fun newInstance() = AssetFragment()

        override fun onReceive(p0: Context?, p1: Intent?) {
            Log.i("资产列表", "新区块到达，更新资产")
            AssetState.fetch()
        }
    }
}
