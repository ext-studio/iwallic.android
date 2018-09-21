package com.iwallic.app.pages.transaction

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.iwallic.app.R
import com.iwallic.app.adapters.TransactionAdapter
import com.iwallic.app.base.BaseLazyFragment
import com.iwallic.app.pages.common.BrowserActivity
import com.iwallic.app.states.TransactionState
import com.iwallic.app.utils.DialogUtils
import com.scwang.smartrefresh.layout.SmartRefreshLayout

class TxConfirmedFragment : BaseLazyFragment() {
    var loader: Dialog? = null
    private lateinit var txRV: RecyclerView
    private lateinit var txSRL: SmartRefreshLayout
    private lateinit var txAdapter: TransactionAdapter
    private lateinit var txManager: LinearLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tx_confirmed, container, false)
        initDOM(view)
        initListener()
        notifyCreatedView()
        return view
    }

    override fun onResolve() {
        initList()
    }

    private fun initDOM(view: View) {
        txRV = view.findViewById(R.id.transaction_list)
        txSRL = view.findViewById(R.id.transaction_pager)

        txAdapter = TransactionAdapter(arrayListOf())
        txManager = LinearLayoutManager(context!!)
        txRV.layoutManager = txManager
        txRV.adapter = txAdapter
    }

    private fun initListener() {
        txSRL.setOnRefreshListener {
            loader = DialogUtils.loader(context!!)
            initList(true)
        }
        txSRL.setOnLoadMoreListener {
            initList(isNext = true)
        }
        txAdapter.onEnter().subscribe {
//            val intent = Intent(context, TransactionDetailActivity::class.java)
//            intent.putExtra("txid", txAdapter.getItem(it).txid)
//            context!!.startActivity(intent)
            val intent = Intent(context, BrowserActivity::class.java)
            intent.putExtra("url", "https://blolys.com/#/transaction/${txAdapter.getItem(it).txid}")
            context?.startActivity(intent)
        }
        txAdapter.onCopy().subscribe {
            copy(txAdapter.getItem(it).txid, "txid")
            vibrate()
        }
    }

    private fun initList(force: Boolean = false, isNext: Boolean = false) {
        if (!isNext) {
            TransactionState.refresh(context, "", force).subscribe({
                loader?.dismiss()
                txAdapter.set(it)
                txSRL.finishRefresh(true)
            }, {
                loader?.dismiss()
                val code = try {it.message?.toInt()?:99999}catch (_: Throwable){99999}
                if (!DialogUtils.error(context, code)) {
                    Toast.makeText(context, "$code", Toast.LENGTH_SHORT).show()
                }
                txSRL.finishRefresh()
            })
        } else {
            TransactionState.next(context, "").subscribe({
                loader?.dismiss()
                txAdapter.push(it)
                if (it.isEmpty()) {
                    txSRL.finishLoadMoreWithNoMoreData()
                } else {
                    txSRL.finishLoadMore(true)
                }
            }, {
                loader?.dismiss()
                val code = try {it.message?.toInt()?:99999}catch (_: Throwable){99999}
                if (!DialogUtils.error(context, code)) {
                    Toast.makeText(context, "$code", Toast.LENGTH_SHORT).show()
                }
                txSRL.finishLoadMore()
            })
        }
    }
}
