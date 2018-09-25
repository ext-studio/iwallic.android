package com.iwallic.app.pages.asset

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
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.iwallic.app.adapters.AssetManageAdapter
import com.iwallic.app.broadcasts.AssetBroadCast
import com.iwallic.app.models.PageDataPyModel
import com.iwallic.app.states.AssetManageState
import com.iwallic.app.states.AssetState
import com.iwallic.app.utils.*
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import io.reactivex.disposables.Disposable

class AssetManageActivity : BaseActivity() {
    private lateinit var backTV: TextView
    private lateinit var amRV: RecyclerView
    private lateinit var amSRL: SmartRefreshLayout
    private lateinit var amAdapter: AssetManageAdapter
    private lateinit var amManager: LinearLayoutManager
    private var changed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset_manage)
        initDOM()
        initListener()
        AssetManageState.init(this, {
            amAdapter.set(it)
        }, {
            if (!DialogUtils.error(this, it)) {
                Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initDOM() {
        backTV = findViewById(R.id.asset_manage_back)
        amRV = findViewById(R.id.asset_manage_list)
        amSRL = findViewById(R.id.asset_manage_pager)
        amManager = LinearLayoutManager(this)
        amAdapter = AssetManageAdapter(arrayListOf(), AssetManageState.watch(this))
        amRV.layoutManager = amManager
        amRV.adapter = amAdapter
    }

    private fun initListener() {
        backTV.setOnClickListener {
            finish()
        }
        amSRL.setOnRefreshListener { _ ->
            AssetManageState.refresh(this, {
                amAdapter.set(it)
                amSRL.finishRefresh(true)
            }, {
                if (!DialogUtils.error(this, it)) {
                    Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                }
                amSRL.finishRefresh()
            })
        }
        amSRL.setOnLoadMoreListener { _ ->
            AssetManageState.older(this, {
                amAdapter.push(it)
                if (it.size > 0) {
                    amSRL.finishLoadMore(true)
                } else {
                    amSRL.finishLoadMoreWithNoMoreData()
                }
            }, {
                if (!DialogUtils.error(this, it)) {
                    Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                }
                amSRL.finishLoadMore()
            })
        }
        amAdapter.setOnToggle { position: Int, state ->
            if (state) {
                AssetManageState.addWatch(this, amAdapter.getAsset(position))
            } else {
                AssetManageState.rmWatch(this, amAdapter.getAsset(position))
            }
            changed = true
        }
    }

    override fun onDestroy() {
        if (changed) {
            AssetBroadCast.send(this)
        }
        super.onDestroy()
    }
}
