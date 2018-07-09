package com.iwallic.app.navigator


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import com.google.gson.Gson

import com.iwallic.app.R
import com.iwallic.app.adapters.AssetAdapter
import com.iwallic.app.models.addrassets
import com.iwallic.app.states.AssetState
import com.iwallic.app.utils.WalletUtils
import io.reactivex.functions.Consumer

class AssetFragment : Fragment() {
    var assetLV: ListView? = null

    companion object {
        val TAG: String = AssetFragment::class.java.simpleName
        fun newInstance() = AssetFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_asset, container, false)
        assetLV = view.findViewById(R.id.asset_list)

        AssetState.list(WalletUtils.address(context!!)).subscribe(Consumer {resolveList(it)})
        AssetState.error().subscribe(Consumer {
            Toast.makeText(context!!, it.toString(), Toast.LENGTH_SHORT).show()
        })
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
    private fun resolveList(list: ArrayList<addrassets>) {
        val adapter = AssetAdapter(context!!, R.layout.adapter_asset_list, list)
        assetLV!!.adapter = adapter
    }
}
