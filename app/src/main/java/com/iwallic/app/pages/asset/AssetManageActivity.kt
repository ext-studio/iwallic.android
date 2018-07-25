package com.iwallic.app.pages.asset

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.iwallic.app.adapters.AssetManageAdapter
import com.iwallic.app.models.AssetRes
import com.iwallic.app.models.PageDataPyModel
import android.content.Intent
import com.iwallic.app.states.AssetState
import com.iwallic.app.utils.*


class AssetManageActivity : BaseActivity() {
    private lateinit var backIV: LinearLayout
    private lateinit var loadPB: ProgressBar
    private lateinit var amRV: RecyclerView
    private lateinit var amSRL: SwipeRefreshLayout
    private lateinit var amAdapter: AssetManageAdapter
    private lateinit var amManager: LinearLayoutManager

    private var fetching: Boolean = false
    private val gson = Gson()
    private lateinit var address: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset_manage)
        initDOM()
        initListener()
        address = WalletUtils.address(this)
        resolveFetch()
    }

    private fun initDOM() {
        backIV = findViewById(R.id.asset_manage_back)
        amRV = findViewById(R.id.asset_manage_list)
        amSRL = findViewById(R.id.asset_manage_list_refresh)
        loadPB = findViewById(R.id.asset_manage_load)
        amSRL.setColorSchemeResources(R.color.colorPrimaryDefault)
        amManager = LinearLayoutManager(this)
        amAdapter = AssetManageAdapter(PageDataPyModel(), SharedPrefUtils.getAsset(this))
        amRV.layoutManager = amManager
        amRV.adapter = amAdapter
    }

    private fun initListener() {
        backIV.setOnClickListener {
//            val intent = Intent()
//            intent.putExtra("assetManage", true)
//            setResult(CommonUtils.CODE_ASSET_MANAGE, intent)
            finish()
        }
        amRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!fetching && newState == 1 && amAdapter.checkNext(amManager.findLastVisibleItemPosition())) {
                    amAdapter.setPaging()
                    resolveFetch(amAdapter.getPage()+1)
                }
            }
        })
        amSRL.setOnRefreshListener {
            resolveFetch()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AssetState.touch()
    }

    private fun resolveRefreshed(success: Boolean = false) {
        if (loadPB.visibility == View.VISIBLE) {
            loadPB.visibility = View.GONE
        }
        if (!amSRL.isRefreshing) {
            return
        }
        amSRL.isRefreshing = false
        if (success) {
            Toast.makeText(this, R.string.toast_refreshed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun resolveFetch(page: Int = 1) {
        if (fetching) {
            return
        }
        fetching = true
        HttpUtils.getPy("/client/assets/list?page=$page&wallet_address=$address", {
            val rs = gson.fromJson<PageDataPyModel<AssetRes>>(it, object: TypeToken<PageDataPyModel<AssetRes>>() {}.type)
            if (rs == null) {
                DialogUtils.error(this, 99998)
                resolveRefreshed()
            } else {
                amAdapter.push(rs)
                resolveRefreshed(true)
            }
            fetching = false
        }, {
            if (!DialogUtils.error(this, it)) {
                Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
            }
            resolveRefreshed()
            fetching = false
        })
    }
}
