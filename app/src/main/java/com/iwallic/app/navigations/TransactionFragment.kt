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
import android.widget.ProgressBar
import android.widget.Toast

import com.iwallic.app.R
import com.iwallic.app.utils.WalletUtils
import com.iwallic.app.adapters.TransactionAdapter
import com.iwallic.app.models.transactions
import com.iwallic.app.services.new_block_action
import com.iwallic.app.states.TransactionState
import com.iwallic.app.utils.DialogUtils
import io.reactivex.disposables.Disposable

class TransactionFragment : Fragment() {
    private lateinit var txRV: RecyclerView
    private lateinit var txSRL: SwipeRefreshLayout
    private lateinit var loadPB: ProgressBar
    private lateinit var txAdapter: RecyclerView.Adapter<*>
    private lateinit var txManager: RecyclerView.LayoutManager

    private lateinit var listListen: Disposable
    private lateinit var errorListen: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)

        txRV = view.findViewById(R.id.transaction_list)
        txSRL = view.findViewById(R.id.transaction_list_refresh)
        loadPB = view.findViewById(R.id.fragment_transaction_load)
        txSRL.setColorSchemeResources(R.color.colorPrimaryDefault)

        resolveList(arrayListOf())

        listListen = TransactionState.list(WalletUtils.address(context!!), "").subscribe({
            resolveList(it.data)
            resolveRefreshed(true)
        }, {
            resolveRefreshed()
            Log.i("交易列表", "发生错误【${it}】")
        })
        errorListen = TransactionState.error().subscribe({
            resolveRefreshed()
            if (!DialogUtils.Error(context!!, it)) {
                Toast.makeText(context!!, it.toString(), Toast.LENGTH_SHORT).show()
            }
        }, {
            resolveRefreshed()
            Log.i("交易列表", "发生错误【${it}】")
        })

        txSRL.setOnRefreshListener {
            if (TransactionState.fetching) {
                txSRL.isRefreshing = false
                return@setOnRefreshListener
            }
            TransactionState.fetch(asset = "")
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

    private fun resolveList(list: ArrayList<transactions>) {
        txManager = LinearLayoutManager(context!!)
        txAdapter = TransactionAdapter(list)
        txRV.apply {
            setHasFixedSize(true)
            layoutManager = txManager
            adapter = txAdapter
        }
    }

    private fun resolveRefreshed(success: Boolean = false) {
        if (loadPB.visibility == View.VISIBLE) {
            loadPB.visibility = View.GONE
        }
        if (!txSRL.isRefreshing) {
            return
        }
        txSRL.isRefreshing = false
        if (success) {
            Toast.makeText(context!!, "数据已更新", Toast.LENGTH_SHORT).show()
        }
    }

    companion object BlockListener: BroadcastReceiver() {
        val TAG: String = TransactionFragment::class.java.simpleName
        fun newInstance() = TransactionFragment()

        override fun onReceive(p0: Context?, p1: Intent?) {
            TransactionState.fetch("", "", silent = true)
        }
    }
}
