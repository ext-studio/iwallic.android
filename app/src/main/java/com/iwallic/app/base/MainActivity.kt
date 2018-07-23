package com.iwallic.app.base

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import com.iwallic.app.navigations.*
import com.iwallic.app.R
import com.iwallic.app.pages.transaction.TransactionReceiveActivity
import com.iwallic.app.pages.transaction.TransactionTransferActivity
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var navFAB: FloatingActionButton
    private lateinit var sendFAB: FloatingActionButton
    private lateinit var receiveFAB: FloatingActionButton

    private val currentNavigation = "currentNavigation"
    private var navPosition: NavigationPosition = NavigationPosition.ASSET
    private var fabOpened: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        restoreSaveInstanceState(savedInstanceState)
        setContentView(R.layout.activity_base_main)

        initDOM()
        initNavigation()
        initFragment(savedInstanceState)
        initFAB()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt(currentNavigation, navPosition.id)
        super.onSaveInstanceState(outState)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        navPosition = findNavigationPositionById(item.itemId)
        return switchFragment(navPosition)
    }

    // move to back when back button tapped
    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun initDOM() {
        navFAB = findViewById(R.id.base_main_fab)
        sendFAB = findViewById(R.id.base_main_fab_send)
        receiveFAB = findViewById(R.id.base_main_fab_receive)
        bottomNavigation = findViewById(R.id.base_main_navigation)
    }
    private fun initFAB() {
        navFAB.bringToFront()
        navFAB.setOnClickListener {
            if (fabOpened) {
                resolveFABClose()
            } else {
                resolveFABOpen()
                launch {
                    delay(2500)
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

            navFAB.animation = AnimationUtils.loadAnimation(this, R.anim.rotate_0deg)
            navFAB.animation.fillAfter = true
            startActivity(Intent(this, TransactionTransferActivity::class.java))
        }
        receiveFAB.setOnClickListener {
            sendFAB.visibility = View.INVISIBLE
            receiveFAB.visibility = View.INVISIBLE

            navFAB.animation = AnimationUtils.loadAnimation(this, R.anim.rotate_0deg)
            navFAB.animation.fillAfter = true
            startActivity(Intent(this, TransactionReceiveActivity::class.java))
        }
    }

    private fun resolveFABOpen() {
        sendFAB.visibility = View.VISIBLE
        receiveFAB.visibility = View.VISIBLE

        navFAB.animation = AnimationUtils.loadAnimation(this, R.anim.rotate_45deg)
        navFAB.animation.fillAfter = true
        fabOpened = true
    }

    private fun resolveFABClose() {
        sendFAB.visibility = View.INVISIBLE
        receiveFAB.visibility = View.INVISIBLE

        navFAB.animation = AnimationUtils.loadAnimation(this, R.anim.rotate_0deg)
        navFAB.animation.fillAfter = true
        fabOpened = false
    }

    private fun restoreSaveInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.also {
            val id = it.getInt(currentNavigation, NavigationPosition.ASSET.id)
            navPosition = findNavigationPositionById(id)
        }
    }

    private fun initNavigation() {
        bottomNavigation.disableShiftMode() // Extension function
        bottomNavigation.active(navPosition.position)   // Extension function
        bottomNavigation.setOnNavigationItemSelectedListener(this)
    }

    private fun initFragment(savedInstanceState: Bundle?) {
        savedInstanceState ?: switchFragment(NavigationPosition.ASSET)
    }

    private fun switchFragment(navPosition: NavigationPosition): Boolean {
        val fragment = supportFragmentManager.findFragment(navPosition)
        if (fragment.isAdded) return false
        detachFragment()
        attachFragment(fragment, navPosition.getTag())
        supportFragmentManager.executePendingTransactions()
        return true
    }

    private fun FragmentManager.findFragment(position: NavigationPosition): Fragment {
        return findFragmentByTag(position.getTag()) ?: position.createFragment()
    }

    private fun detachFragment() {
        supportFragmentManager.findFragmentById(R.id.container)?.also {
            supportFragmentManager.beginTransaction().detach(it).commit()
        }
    }

    private fun attachFragment(fragment: Fragment, tag: String) {
        if (fragment.isDetached) {
            supportFragmentManager.beginTransaction().attach(fragment).commit()
        } else {
            supportFragmentManager.beginTransaction().add(R.id.container, fragment, tag).commit()
        }
        supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
    }
}
