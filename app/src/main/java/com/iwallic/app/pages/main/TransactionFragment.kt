package com.iwallic.app.pages.main

import android.content.*
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.adapters.TransactionAdapter
import com.iwallic.app.base.BaseFragment
import com.iwallic.app.models.PageDataPyModel
import com.iwallic.app.pages.transaction.TransactionDetailActivity
import com.iwallic.app.pages.transaction.TransactionUnconfirmedActivity
import com.iwallic.app.states.TransactionState
import com.iwallic.app.utils.CommonUtils
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.WalletUtils
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import io.reactivex.disposables.Disposable

class TransactionFragment : BaseFragment() {
    private lateinit var txRV: RecyclerView
    private lateinit var txSRL: SmartRefreshLayout
    // private lateinit var loadPB: ProgressBar
    private lateinit var txAdapter: TransactionAdapter
    private lateinit var txManager: LinearLayoutManager
    private lateinit var unconfirmedLL: TextView

    private lateinit var listListen: Disposable
    private lateinit var errorListen: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)
        initDOM(view)
        initListener()
        context!!.registerReceiver(BlockListener, IntentFilter(CommonUtils.ACTION_NEWBLOCK))
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listListen.dispose()
        errorListen.dispose()
        context!!.unregisterReceiver(BlockListener)
    }

    private fun initDOM(view: View) {
        txRV = view.findViewById(R.id.transaction_list)
        txSRL = view.findViewById(R.id.transaction_pager)
        // loadPB = view.findViewById(R.id.fragment_transaction_load)
        unconfirmedLL = view.findViewById(R.id.transaction_unconfirmed_enter)

        setStatusBar(view.findViewById(R.id.app_top_space))

        txAdapter = TransactionAdapter(PageDataPyModel())
        txManager = LinearLayoutManager(context!!)
        txRV.layoutManager = txManager
        txRV.adapter = txAdapter

        txSRL.setEnableOverScrollDrag(true)
    }

    private fun initListener() {
        listListen = TransactionState.list(WalletUtils.address(context!!), "").subscribe({
            txAdapter.push(it)
            txSRL.finishRefresh(true)
            if (it.page >= it.pages) {
                txSRL.finishLoadMoreWithNoMoreData()
            } else {
                txSRL.finishLoadMore(true)
            }
        }, {
            txSRL.finishRefresh(false)
            txSRL.finishLoadMore(false)
            Log.i("【TxList】", "error【${it}】")
        })
        errorListen = TransactionState.error().subscribe({
            txSRL.finishRefresh(false)
            txSRL.finishLoadMore(false)
            if (!DialogUtils.error(context!!, it)) {
                Toast.makeText(context!!, it.toString(), Toast.LENGTH_SHORT).show()
            }
        }, {
            txSRL.finishRefresh(false)
            txSRL.finishLoadMore(false)
            Log.i("【TxList】", "error【${it}】")
        })
        txSRL.setOnRefreshListener {
            TransactionState.fetch()
        }
        txSRL.setOnLoadMoreListener {
            TransactionState.next()
        }
//        txSRL.setOnRefreshListener {
//            if (TransactionState.fetching) {
//                txSRL.isRefreshing = false
//                return@setOnRefreshListener
//            }
//            TransactionState.fetch(asset = "")
//        }
//        txRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (!TransactionState.fetching && newState == 1 && txAdapter.checkNext(txManager.findLastVisibleItemPosition())) {
//                    txAdapter.setPaging()
//                    TransactionState.next()
//                }
//            }
//        })
        txAdapter.onEnter().subscribe {
            val intent = Intent(context, TransactionDetailActivity::class.java)
            intent.putExtra("txid", txAdapter.getItem(it).txid)
            context!!.startActivity(intent)
        }
        txAdapter.onCopy().subscribe {
            copy(txAdapter.getItem(it).txid, "txid")
            vibrate()
        }
        unconfirmedLL.setOnClickListener {
            context!!.startActivity(Intent(context, TransactionUnconfirmedActivity::class.java))
        }
    }

//    private fun resolveRefreshed(success: Boolean = false) {
//        if (loadPB.visibility == View.VISIBLE) {
//            loadPB.visibility = View.GONE
//        }
//        if (!txSRL.isRefreshing) {
//            return
//        }
//        txSRL.isRefreshing = false
//        if (success) {
//            Toast.makeText(context!!, R.string.toast_refreshed, Toast.LENGTH_SHORT).show()
//        }
//    }

    companion object BlockListener: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            // TransactionState.fetch("", "", silent = true)
        }
    }
}