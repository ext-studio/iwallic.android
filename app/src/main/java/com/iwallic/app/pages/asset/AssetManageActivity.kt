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
    private lateinit var amRV: RecyclerView
    private lateinit var amSRL: SmartRefreshLayout
    private lateinit var amAdapter: AssetManageAdapter
    private lateinit var amManager: LinearLayoutManager

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
        listListen = AssetManageState.list(WalletUtils.address(this)).subscribe({
            amAdapter.push(it)
            amSRL.finishRefresh(true)
            if (it.page == it.pages) {
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
            amSRL.finishRefresh()
            amSRL.finishLoadMore()
        }, {
            Log.i("【AssetManage】", "error【$it】")
            amSRL.finishRefresh()
            amSRL.finishLoadMore()
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
        listListen.dispose()
        errorListen.dispose()
    }
}
