package com.iwallic.app.pages.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
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
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show()
            launch (UI) {
                delay(1500)
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
        toggleFAB = findViewById(R.id.main_toggle)
        sendFAB = findViewById(R.id.main_send)
        receiveFAB = findViewById(R.id.main_receive)
        navBNV = findViewById(R.id.main_navigation)
    }
    private fun initNav() {
        navBNV.disableShiftMode()

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

    private fun resolveNavPage(id: Int): BaseFragment {
        return when(id) {
            R.id.menu_main_asset -> AssetFragment()
            R.id.menu_main_transaction -> TransactionFragment()
            R.id.menu_main_find -> FindFragment()
            R.id.menu_main_user -> UserFragment()
            else -> AssetFragment()
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
