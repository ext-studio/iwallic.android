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
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.base.BaseFragment
import com.iwallic.app.base.BaseFragmentAdapter
import com.iwallic.app.components.NoSwipeViewPager
import com.iwallic.app.models.Pager
import com.iwallic.app.pages.transaction.TransactionReceiveActivity
import com.iwallic.app.pages.transaction.TransactionTransferActivity
import com.iwallic.app.services.BlockService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var navBNV: BottomNavigationView
    private lateinit var toggleIV: ImageView
    private lateinit var pagerNSVP: NoSwipeViewPager
    private lateinit var adapter: BaseFragmentAdapter

    private var canExit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initDOM()
        initNav()
        initFAB()

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
        pagerNSVP = findViewById(R.id.main_pager)
        toggleIV = findViewById(R.id.main_toggle)
        navBNV = findViewById(R.id.main_navigation)
    }
    private fun initNav() {
        adapter = BaseFragmentAdapter(supportFragmentManager)
        adapter.setPage(Pager(R.id.menu_main_asset, AssetFragment()))

        pagerNSVP.adapter = adapter
        pagerNSVP.currentItem = 0
        pagerNSVP.offscreenPageLimit = 4
        navBNV.setOnNavigationItemSelectedListener {
            val position = adapter.getPosition(it.itemId)
            if (position < 0) {
                pagerNSVP.setCurrentItem(adapter.setPage(Pager(it.itemId, resolveNavPage(it.itemId))), false)
            } else {
                pagerNSVP.setCurrentItem(position, false)
            }
            true
        }
    }
    private fun initFAB() {
        toggleIV.setOnClickListener { _ ->
            val dialog = BottomSheetDialog(this)

            val view = View.inflate(this, R.layout.dialog_main_toggle, null)
            val transfer = view.findViewById<FrameLayout>(R.id.dialog_main_send)
            val receive = view.findViewById<FrameLayout>(R.id.dialog_main_receive)
            view.setOnClickListener {
                dialog.dismiss()
            }
            transfer.setOnClickListener {
                startActivity(Intent(this, TransactionTransferActivity::class.java))
                dialog.dismiss()
            }
            receive.setOnClickListener {
                startActivity(Intent(this, TransactionReceiveActivity::class.java))
                dialog.dismiss()
            }
            dialog.setContentView(view)
            dialog.show()
        }
    }

    private fun resolveNavPage(id: Int): BaseFragment {
        return when(id) {
            R.id.menu_main_asset -> AssetFragment()
            R.id.menu_main_transaction -> TransactionFragment()
            R.id.menu_main_find -> FindFragment()
            R.id.menu_main_user -> UserFragment()
            else -> AssetFragment()
        }
    }
}
