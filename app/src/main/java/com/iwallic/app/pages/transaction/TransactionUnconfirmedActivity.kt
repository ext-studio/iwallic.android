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
    // private lateinit var unPB: ProgressBar

    private lateinit var unManager: LinearLayoutManager
    private lateinit var unAdapter: TransactionAdapter

    private lateinit var listen: Disposable
    private lateinit var error: Disposable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_unconfirmed)
        initDOM()
        initListen()
    }

    override fun onDestroy() {
        super.onDestroy()
        listen.dispose()
        error.dispose()
    }

    private fun initDOM() {
        backLL = findViewById(R.id.transaction_unconfirmed)
        // unPB = findViewById(R.id.transaction_unconfirmed_load)
        unSRL = findViewById(R.id.transaction_unconfirmed_pager)
        unRV = findViewById(R.id.transaction_unconfirmed_list)

        // unSRL.setColorSchemeResources(R.color.colorPrimaryDefault)

        unManager = LinearLayoutManager(this)
        unAdapter = TransactionAdapter(PageDataPyModel())
        unRV.layoutManager = unManager
        unRV.adapter = unAdapter

        unSRL.setEnableOverScrollDrag(true)
    }
    private fun initListen() {
        listen = UnconfirmedState.list(this, WalletUtils.address(this)).subscribe({
            unAdapter.push(it)
            if (it.page == 1) {
                unRV.scrollToPosition(0)
            }
            unSRL.finishRefresh(true)
            if (it.items.size >= it.total) {
                unSRL.finishLoadMoreWithNoMoreData()
            } else {
                unSRL.finishLoadMore(true)
            }
        }, {
            unSRL.finishRefresh(false)
            unSRL.finishLoadMore(false)
            Log.i("【Unconfirmed】", "error【$it】")
        })
        error = UnconfirmedState.error().subscribe({
            if (!DialogUtils.error(this, it)) {
                Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
            }
            unSRL.finishRefresh(false)
            unSRL.finishLoadMore(false)
        }, {
            unSRL.finishRefresh(false)
            unSRL.finishLoadMore(false)
            Log.i("【Unconfirmed】", "error【$it】")
        })
//        unRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (!UnconfirmedState.fetching && newState == 1 && unAdapter.checkNext(unManager.findLastVisibleItemPosition())) {
//                    unAdapter.setPaging()
//                    UnconfirmedState.next()
//                }
//            }
//        })
//        unSRL.setOnRefreshListener {
//            if (UnconfirmedState.fetching) {
//                unSRL.isRefreshing = false
//                return@setOnRefreshListener
//            }
//            UnconfirmedState.fetch()
//        }
        unAdapter.onCopy().subscribe {
            copy(unAdapter.getItem(it).txid, "txid")
            vibrate()
        }
        backLL.setOnClickListener {
            finish()
        }
        unSRL.setOnRefreshListener {
            UnconfirmedState.fetch(this)
        }
        unSRL.setOnLoadMoreListener {
            UnconfirmedState.next(this)
        }
    }

//    private fun resolveRefreshed(success: Boolean = false) {
//        if (unPB.visibility == View.VISIBLE) {
//            unPB.visibility = View.GONE
//        }
//        if (!unSRL.isRefreshing) {
//            return
//        }
//        unSRL.isRefreshing = false
//        if (success) {
//            Toast.makeText(this, R.string.toast_refreshed, Toast.LENGTH_SHORT).show()
//        }
//    }
}
