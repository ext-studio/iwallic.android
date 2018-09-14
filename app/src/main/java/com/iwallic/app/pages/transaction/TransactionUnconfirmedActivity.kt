package com.iwallic.app.pages.transaction

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.adapters.TransactionAdapter
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.models.PageDataPyModel
import com.iwallic.app.models.SmartContract
import com.iwallic.app.states.UnconfirmedState
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.WalletUtils
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import io.reactivex.disposables.Disposable

class TransactionUnconfirmedActivity : BaseActivity() {
    private lateinit var backLL: TextView
    private lateinit var unRV: RecyclerView
    private lateinit var unSRL: SmartRefreshLayout

    private lateinit var unManager: LinearLayoutManager
    private lateinit var unAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_unconfirmed)
        initDOM()
        initListen()
        initList()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun initDOM() {
        backLL = findViewById(R.id.transaction_unconfirmed)
        unSRL = findViewById(R.id.transaction_unconfirmed_pager)
        unRV = findViewById(R.id.transaction_unconfirmed_list)

        unManager = LinearLayoutManager(this)
        unAdapter = TransactionAdapter(arrayListOf())
        unRV.layoutManager = unManager
        unRV.adapter = unAdapter
    }
    private fun initListen() {
        unAdapter.onCopy().subscribe {
            copy(unAdapter.getItem(it).txid, "txid")
            vibrate()
        }
        backLL.setOnClickListener {
            finish()
        }
        unSRL.setOnRefreshListener {
            initList(true)
        }
        unSRL.setOnLoadMoreListener {
            initList(isNext = true)
        }
    }

    private fun initList(force: Boolean = false, isNext: Boolean = false) {
        if (!isNext) {
            UnconfirmedState.refresh(this, force).subscribe({
                unAdapter.set(it)
                unSRL.finishRefresh(true)
            }, {
                val code = try {it.message?.toInt()?:99999}catch (_: Throwable){99999}
                if (!DialogUtils.error(this, code)) {
                    Toast.makeText(this, "$code", Toast.LENGTH_SHORT).show()
                }
                unSRL.finishRefresh()
            })
        } else {
            UnconfirmedState.next(this).subscribe({
                unAdapter.push(it)
                if (it.isEmpty()) {
                    unSRL.finishLoadMoreWithNoMoreData()
                } else {
                    unSRL.finishLoadMore(true)
                }
            }, {
                val code = try {it.message?.toInt()?:99999}catch (_: Throwable){99999}
                if (!DialogUtils.error(this, code)) {
                    Toast.makeText(this, "$code", Toast.LENGTH_SHORT).show()
                }
                unSRL.finishLoadMore()
            })
        }
    }
}
