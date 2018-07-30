package com.iwallic.app.pages.asset

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.iwallic.app.adapters.AssetManageAdapter
import com.iwallic.app.models.PageDataPyModel
import com.iwallic.app.states.AssetManageState
import com.iwallic.app.states.AssetState
import com.iwallic.app.utils.*
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import io.reactivex.disposables.Disposable


class AssetManageActivity : BaseActivity() {
    private lateinit var backIV: LinearLayout
    // private lateinit var loadPB: ProgressBar
    private lateinit var amRV: RecyclerView
    private lateinit var amSRL: SmartRefreshLayout
    private lateinit var amAdapter: AssetManageAdapter
    private lateinit var amManager: LinearLayoutManager

    private var fetching: Boolean = false
    private lateinit var address: String
    private lateinit var listListen: Disposable
    private lateinit var errorListen: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset_manage)
        initDOM()
        initListener()
        address = WalletUtils.address(this)
    }

    private fun initDOM() {
        backIV = findViewById(R.id.asset_manage_back)
        amRV = findViewById(R.id.asset_manage_list)
        amSRL = findViewById(R.id.asset_manage_pager)
        // loadPB = findViewById(R.id.asset_manage_load)
        // amSRL.setColorSchemeResources(R.color.colorPrimaryDefault)
        amManager = LinearLayoutManager(this)
        amAdapter = AssetManageAdapter(PageDataPyModel(), SharedPrefUtils.getAsset(this))
        amRV.layoutManager = amManager
        amRV.adapter = amAdapter

        amSRL.setEnableOverScrollDrag(true)
    }

    private fun initListener() {
        backIV.setOnClickListener {
            finish()
        }
//        amRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (!fetching && newState == 1 && amAdapter.checkNext(amManager.findLastVisibleItemPosition())) {
//                    amAdapter.setPaging()
//                    AssetManageState.next()
//                }
//            }
//        })
//        amSRL.setOnRefreshListener {
//            AssetManageState.fetch()
//        }
        listListen = AssetManageState.list(WalletUtils.address(this)).subscribe({
            amAdapter.push(it)
            if (it.page == 1) {
                amRV.scrollToPosition(0)
            }
            amSRL.finishRefresh(true)
            if (it.items.size >= it.total) {
                amSRL.finishLoadMoreWithNoMoreData()
            } else {
                amSRL.finishLoadMore(true)
            }
        }, {
            Log.i("【AssetManage】", "error【$it】")
            amSRL.finishRefresh(false)
            amSRL.finishLoadMore(false)
        })
        errorListen = AssetManageState.error().subscribe({
            if (!DialogUtils.error(this, it)) {
                Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
            }
            amSRL.finishRefresh(false)
            amSRL.finishLoadMore(false)
        }, {
            Log.i("【AssetManage】", "error【$it】")
            amSRL.finishRefresh(false)
            amSRL.finishLoadMore(false)
        })
        amSRL.setOnRefreshListener {
            AssetManageState.fetch()
        }
        amSRL.setOnLoadMoreListener {
            AssetManageState.next()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AssetState.touch()
    }

//    private fun resolveRefreshed(success: Boolean = false) {
//        if (loadPB.visibility == View.VISIBLE) {
//            loadPB.visibility = View.GONE
//        }
//        if (!amSRL.isRefreshing) {
//            return
//        }
//        amSRL.isRefreshing = false
//        if (success) {
//            Toast.makeText(this, R.string.toast_refreshed, Toast.LENGTH_SHORT).show()
//        }
//    }
}
