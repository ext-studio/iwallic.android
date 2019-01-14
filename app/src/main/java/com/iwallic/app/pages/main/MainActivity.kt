package com.iwallic.app.pages.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.adapters.AssetAdapter
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.base.BaseFragment
import com.iwallic.app.base.BaseFragmentAdapter
import com.iwallic.app.components.NoSwipeViewPager
import com.iwallic.app.models.AssetRes
import com.iwallic.app.models.Pager
import com.iwallic.app.pages.asset.AssetManageActivity
import com.iwallic.app.pages.transaction.TransactionActivity
import com.iwallic.app.pages.transaction.TransactionReceiveActivity
import com.iwallic.app.pages.transaction.TransactionTransferActivity
import com.iwallic.app.services.BlockService
import com.iwallic.app.states.AssetManageState
import com.iwallic.app.states.AssetState
import com.iwallic.app.utils.CommonUtils
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.SharedPrefUtils
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    private lateinit var mainD: DrawerLayout
    private lateinit var navNV: NavigationView

    private lateinit var topMenuIV: ImageView
    private lateinit var topBalanceTV: TextView
    private lateinit var topNameTV: TextView
    private lateinit var topManageIV: ImageView
    private lateinit var pagerSRL: SmartRefreshLayout
    private lateinit var listRV: RecyclerView
    private lateinit var adapter: AssetAdapter

    private var canExit = false
    private var address = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        address = SharedPrefUtils.getAddress(this)
        initDOM()
        initNav()
        initListen()
        initList()
        startService(Intent(this, BlockService::class.java))
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle?) {
//        super.onSaveInstanceState(outState)
    }
    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
//        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onBackPressed() {
        if (mainD.isDrawerOpen(GravityCompat.START)) {
            mainD.closeDrawer(GravityCompat.START)
            return
        }
        if (!canExit) {
            canExit = true
            Toast.makeText(this, R.string.main_exit, Toast.LENGTH_SHORT).show()
           GlobalScope.launch (Dispatchers.Main) {
                delay(1500L)
                canExit = false
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, BlockService::class.java))
    }

    private fun initDOM() {
        mainD = findViewById(R.id.main_drawer)
        navNV = findViewById(R.id.main_navigation)

        topBalanceTV = findViewById(R.id.main_top_balance)
        topManageIV = findViewById(R.id.main_top_manage)
        topMenuIV = findViewById(R.id.main_top_menu)
        topNameTV = findViewById(R.id.main_top_name)
        pagerSRL = findViewById(R.id.main_pager)
        listRV = findViewById(R.id.main_list)

        adapter = AssetAdapter(arrayListOf())
        listRV.layoutManager = LinearLayoutManager(this)
        listRV.adapter = adapter
    }
    private fun initListen() {
        pagerSRL.setOnRefreshListener {
            initList(true)
        }
        topMenuIV.setOnClickListener {
            mainD.openDrawer(Gravity.START)
        }
        topManageIV.setOnClickListener {
            val intent = Intent(this, AssetManageActivity::class.java)
            startActivity(intent)
        }
    }
    private fun initList(force: Boolean = false) {
        AssetState.list(this, address, force, {
            resolveList(it)
            pagerSRL.finishRefresh()
        }, {
            pagerSRL.finishRefresh()
            if (!DialogUtils.error(this, it)) {
                DialogUtils.toast(this, "Error:$it")
            }
        })
    }
    private fun initNav() {
        navNV.itemIconTintList = null
        navNV.setNavigationItemSelectedListener {
            val intent = when (it.itemId) {
                R.id.menu_main_transaction -> {
                    Intent(this, TransactionActivity::class.java)
                }
                else -> {
                    return@setNavigationItemSelectedListener true
                }
            }
            startActivity(intent)
            mainD.closeDrawer(Gravity.START)
            true
        }
    }

    private fun resolveList(list: ArrayList<AssetRes>) {
        topNameTV.text = "NEO"

        val balance = list.find {
            it.asset_id == CommonUtils.NEO
        }?.balance ?: "0"

        topBalanceTV.text = if (balance == "0.0") "0" else balance

        for (asset in AssetManageState.watch(this)) {
            if (list.indexOfFirst {
                it.asset_id == asset.asset_id
            } < 0) {
                list.add(asset)
            }
        }
        adapter.set(list)
    }
}
