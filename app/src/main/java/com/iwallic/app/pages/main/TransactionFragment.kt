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
    private lateinit var txAdapter: TransactionAdapter
    private lateinit var txManager: LinearLayoutManager
    private lateinit var unconfirmedLL: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)
        initDOM(view)
        initListener()
        initList()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun initDOM(view: View) {
        txRV = view.findViewById(R.id.transaction_list)
        txSRL = view.findViewById(R.id.transaction_pager)
        // loadPB = view.findViewById(R.id.fragment_transaction_load)
        unconfirmedLL = view.findViewById(R.id.transaction_unconfirmed_enter)

        txAdapter = TransactionAdapter(arrayListOf())
        txManager = LinearLayoutManager(context!!)
        txRV.layoutManager = txManager
        txRV.adapter = txAdapter
    }

    private fun initListener() {
        txSRL.setOnRefreshListener {
            initList(true)
        }
        txSRL.setOnLoadMoreListener {
            initList(isNext = true)
        }
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

    private fun initList(force: Boolean = false, isNext: Boolean = false) {
        if (!isNext) {
            TransactionState.refresh(context, "", force).subscribe({
                txAdapter.set(it)
                txSRL.finishRefresh(true)
            }, {
                val code = try {it.message?.toInt()?:99999}catch (_: Throwable){99999}
                if (!DialogUtils.error(context, code)) {
                    Toast.makeText(context, "$code", Toast.LENGTH_SHORT).show()
                }
                txSRL.finishRefresh()
            })
        } else {
            TransactionState.next(context, "").subscribe({
                txAdapter.push(it)
                if (it.isEmpty()) {
                    txSRL.finishLoadMoreWithNoMoreData()
                } else {
                    txSRL.finishLoadMore(true)
                }
            }, {
                val code = try {it.message?.toInt()?:99999}catch (_: Throwable){99999}
                if (!DialogUtils.error(context, code)) {
                    Toast.makeText(context, "$code", Toast.LENGTH_SHORT).show()
                }
                txSRL.finishLoadMore()
            })
        }
    }
}
