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
import com.iwallic.app.utils.WalletUtils
import com.iwallic.app.adapters.TransactionAdapter
import com.iwallic.app.models.pageData
import com.iwallic.app.models.transactions
import com.iwallic.app.services.new_block_action
import com.iwallic.app.states.TransactionState
import io.reactivex.disposables.Disposable

class TransactionFragment : Fragment() {
    private lateinit var txLV: ListView
    private lateinit var txListen: Disposable
    private lateinit var errorListen: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)
        txLV = view.findViewById(R.id.transaction_list)

        txListen = TransactionState.list(WalletUtils.address(context!!)).subscribe({
            resolveList(it)
        }, {
            Log.i("交易列表", "发生错误【${it}】")
        })
        errorListen = TransactionState.error().subscribe({
            Toast.makeText(context!!, it.toString(), Toast.LENGTH_SHORT).show()
        }, {
            Log.i("交易列表", "发生错误【${it}】")
        })

        context!!.registerReceiver(BlockListener, IntentFilter(new_block_action))
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        txListen.dispose()
        errorListen.dispose()
        context!!.unregisterReceiver(BlockListener)
    }

    private fun resolveList(list: pageData<transactions>) {
        val adapter = TransactionAdapter(context!!, R.layout.adapter_transaction_list, list.data)
        txLV.adapter = adapter
    }

    companion object BlockListener: BroadcastReceiver() {
        val TAG: String = TransactionFragment::class.java.simpleName
        fun newInstance() = TransactionFragment()

        override fun onReceive(p0: Context?, p1: Intent?) {
            TransactionState.fetch()
        }
    }
}
