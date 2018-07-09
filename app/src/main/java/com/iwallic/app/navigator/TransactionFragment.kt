package com.iwallic.app.navigator


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.google.gson.Gson

import com.iwallic.app.R
import com.iwallic.app.adapters.AssetAdapter
import com.iwallic.app.utils.HttpClient
import com.iwallic.app.utils.WalletUtils
import com.google.gson.reflect.TypeToken
import com.iwallic.app.adapters.TransactionAdapter
import com.iwallic.app.models.pageData
import com.iwallic.app.models.transactions

class TransactionFragment : Fragment() {
    val gson = Gson()
    var transactionLV: ListView? = null
    companion object {
        val TAG: String = TransactionFragment::class.java.simpleName
        fun newInstance() = TransactionFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)
        transactionLV = view.findViewById(R.id.asset_list)
        HttpClient.post("getaccounttxes", listOf(1, 10, WalletUtils.address(context!!)), fun(res: String) {
            Log.i("交易列表", res)
            val json = gson.fromJson<pageData<transactions>>(res, object: TypeToken<pageData<transactions>>() {}.type)
            resolveList(json)
        }, fun(err) {

        })
//        resolveList(arrayListOf(addrassets("测试1", "99999", "测试1", "测试1"),
//                addrassets("测试1", "99999", "测试2", "测试1"),
//                addrassets("测试1", "99999", "测试3", "测试1"),
//                addrassets("测试1", "99999", "测试4", "测试1"),
//                addrassets("测试1", "99999", "测试5", "测试1")))
        return view
    }

    private fun resolveList(list: pageData<transactions>) {
        Log.i("交易列表", list.data!!.size.toString())
        val adapter = TransactionAdapter(context!!, R.layout.adapter_transaction_list, list.data!!)
        transactionLV!!.adapter = adapter
    }
}
