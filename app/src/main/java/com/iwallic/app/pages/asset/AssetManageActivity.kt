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
import com.iwallic.app.models.assetmanage
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.HttpClient
import com.iwallic.app.utils.WalletUtils

class AssetManageActivity : BaseActivity() {

    private lateinit var backIV: LinearLayout
    private lateinit var loadPB: ProgressBar

    private lateinit var amRV: RecyclerView
    private lateinit var amSRL: SwipeRefreshLayout
    private lateinit var amAdapter: RecyclerView.Adapter<*>
    private lateinit var amManager: RecyclerView.LayoutManager

    private var fetching: Boolean = false
    private val gson = Gson()
    private var cache: ArrayList<assetmanage>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset_manage)

        backIV = findViewById(R.id.asset_manage_back)
        amRV = findViewById(R.id.asset_manage_list)
        amSRL = findViewById(R.id.asset_manage_list_refresh)
        loadPB = findViewById(R.id.asset_manage_load)
        amSRL.setColorSchemeResources(R.color.colorPrimaryDefault)

        resolveList(arrayListOf())
        resolveFetch()
        backIV.setOnClickListener {
            finish()
        }
        amSRL.setOnRefreshListener {
            if (fetching) {
                amSRL.isRefreshing = false
                return@setOnRefreshListener
            }
            resolveFetch()
        }
    }

    private fun resolveList(list: ArrayList<assetmanage>) {
        amManager = LinearLayoutManager(this)
        amAdapter = AssetManageAdapter(list)
        amRV.apply {
            setHasFixedSize(true)
            layoutManager = amManager
            adapter = amAdapter
        }
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
            Toast.makeText(this, "数据已更新", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resolveFetch() {
        if (fetching) {
            return
        }
        fetching = true
        HttpClient.post("getaddrassets", listOf(WalletUtils.address(this), 0), fun (res) {
            fetching = false
            val data = gson.fromJson<ArrayList<assetmanage>>(res, object: TypeToken<ArrayList<assetmanage>>() {}.type)
            if (data == null) {
                if (!DialogUtils.Error(this, 99998)) {
                    Toast.makeText(this, "请求失败", Toast.LENGTH_SHORT).show()
                }
                resolveRefreshed()
            } else {
                cache = data
                resolveList(data)
                resolveRefreshed(true)
            }
        }, fun (err) {
            fetching = false
            if (!DialogUtils.Error(this, err)) {

            }
            resolveRefreshed()
        })
    }
}
