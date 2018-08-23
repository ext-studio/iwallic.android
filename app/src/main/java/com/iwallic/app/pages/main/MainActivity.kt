package com.iwallic.app.pages.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.base.BaseFragmentAdapter
import com.iwallic.app.components.NoSwipeViewPager
import com.iwallic.app.pages.transaction.TransactionReceiveActivity
import com.iwallic.app.pages.transaction.TransactionTransferActivity
import com.iwallic.app.services.BlockService
import com.iwallic.app.utils.CommonUtils
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import java.util.*

class MainActivity : BaseActivity() {

    private lateinit var navBNV: BottomNavigationView
    private lateinit var toggleFAB: FloatingActionButton
    private lateinit var sendFAB: FloatingActionButton
    private lateinit var receiveFAB: FloatingActionButton
    private lateinit var pagerNSVP: NoSwipeViewPager
    private lateinit var adapter: BaseFragmentAdapter

    private var fabOpened = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        setContentView(R.layout.activity_main)

        initDOM()
        initNav()
        initFAB()

        CommonUtils.onConfigured().subscribe({
            if (it) {
                startService(Intent(this, BlockService::class.java))
            }
        }, {
            Log.i("【BaseActivity】", "config failed, block service will not on")
        })
    }

    // move to back when back button tapped
    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, BlockService::class.java))
    }

    private fun initDOM() {
        pagerNSVP = findViewById(R.id.main_pager)
        toggleFAB = findViewById(R.id.main_toggle)
        sendFAB = findViewById(R.id.main_send)
        receiveFAB = findViewById(R.id.main_receive)
        navBNV = findViewById(R.id.main_navigation)
    }
    private fun initNav() {
        navBNV.disableShiftMode()

        adapter = BaseFragmentAdapter(supportFragmentManager)
        adapter.setPage(AssetFragment())
        adapter.setPage(TransactionFragment())
        adapter.setPage(FindFragment())
        adapter.setPage(UserFragment())

        pagerNSVP.adapter = adapter
        pagerNSVP.currentItem = 0
        navBNV.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_main_asset -> pagerNSVP.setCurrentItem(0, false)
                R.id.menu_main_transaction -> pagerNSVP.setCurrentItem(1, false)
                R.id.menu_main_find -> pagerNSVP.setCurrentItem(2, false)
                R.id.menu_main_user -> pagerNSVP.setCurrentItem(3, false)
            }
            true
        }
    }
    private fun initFAB() {
        toggleFAB.bringToFront()
        toggleFAB.setOnClickListener {
            if (fabOpened) {
                resolveFABClose()
            } else {
                resolveFABOpen()
                launch {
                    delay(3000)
                    withContext(UI) {
                        if (fabOpened) {
                            resolveFABClose()
                        }
                    }
                }
            }
        }
        sendFAB.setOnClickListener {
            sendFAB.visibility = View.INVISIBLE
            receiveFAB.visibility = View.INVISIBLE

            toggleFAB.animation = AnimationUtils.loadAnimation(this, R.anim.rotate_0deg)
            toggleFAB.animation.fillAfter = true
            startActivity(Intent(this, TransactionTransferActivity::class.java))
        }
        receiveFAB.setOnClickListener {
            sendFAB.visibility = View.INVISIBLE
            receiveFAB.visibility = View.INVISIBLE

            toggleFAB.animation = AnimationUtils.loadAnimation(this, R.anim.rotate_0deg)
            toggleFAB.animation.fillAfter = true
            startActivity(Intent(this, TransactionReceiveActivity::class.java))
        }
    }

    private fun resolveFABOpen() {
        sendFAB.visibility = View.VISIBLE
        receiveFAB.visibility = View.VISIBLE

        toggleFAB.animation = AnimationUtils.loadAnimation(this, R.anim.rotate_45deg)
        toggleFAB.animation.fillAfter = true
        fabOpened = true
    }

    private fun resolveFABClose() {
        sendFAB.visibility = View.INVISIBLE
        receiveFAB.visibility = View.INVISIBLE

        toggleFAB.animation = AnimationUtils.loadAnimation(this, R.anim.rotate_0deg)
        toggleFAB.animation.fillAfter = true
        fabOpened = false
    }

    @SuppressLint("RestrictedApi")
    fun BottomNavigationView.disableShiftMode() {
        val menuView = getChildAt(0) as BottomNavigationMenuView
        try {
            menuView.javaClass.getDeclaredField("mShiftingMode").also { shiftMode ->
                shiftMode.isAccessible = true
                shiftMode.setBoolean(menuView, false)
                shiftMode.isAccessible = false
            }
            for (i in 0 until menuView.childCount) {
                (menuView.getChildAt(i) as BottomNavigationItemView).also { item ->
                    item.setShiftingMode(false)
                    item.setChecked(item.itemData.isChecked)
                }
            }
        } catch (t: Throwable) {
            Log.e("BottomNavigationHelper", "Unable to get shift mode field", t)
        } catch (e: IllegalAccessException) {
            Log.e("BottomNavigationHelper", "Unable to change value of shift mode", e)
        }
    }
}
