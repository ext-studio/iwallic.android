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
import android.view.animation.AnimationUtils
import com.iwallic.app.navigations.*
import com.iwallic.app.R
import com.iwallic.app.pages.transaction.TransactionReceiveActivity
import com.iwallic.app.pages.transaction.TransactionTransferActivity


class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private val currentNavigation = "currentNavigation"
    private var navPosition: NavigationPosition = NavigationPosition.ASSET
    var bottomNavigation: BottomNavigationView? = null
    var navFAB: FloatingActionButton? = null
    var sendFAB: FloatingActionButton? = null
    var receiveFAB: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        restoreSaveInstanceState(savedInstanceState)
        setContentView(R.layout.activity_base_main)

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

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun initFAB() {
        navFAB = findViewById(R.id.base_main_fab)
        sendFAB = findViewById(R.id.base_main_fab_send)
        receiveFAB = findViewById(R.id.base_main_fab_receive)
        navFAB!!.bringToFront()
        navFAB!!.setOnClickListener {
            if (sendFAB!!.visibility == View.VISIBLE) {
                sendFAB!!.visibility = View.INVISIBLE
                receiveFAB!!.visibility = View.INVISIBLE

                navFAB!!.animation = AnimationUtils.loadAnimation(this, R.anim.rotate_0deg)
                navFAB!!.animation.fillAfter = true
            } else {
                sendFAB!!.visibility = View.VISIBLE
                receiveFAB!!.visibility = View.VISIBLE

                navFAB!!.animation = AnimationUtils.loadAnimation(this, R.anim.rotate_45deg)
                navFAB!!.animation.fillAfter = true
            }
        }
        sendFAB!!.setOnClickListener {
            sendFAB!!.visibility = View.INVISIBLE
            receiveFAB!!.visibility = View.INVISIBLE

            navFAB!!.animation = AnimationUtils.loadAnimation(this, R.anim.rotate_0deg)
            navFAB!!.animation.fillAfter = true
            startActivity(Intent(this, TransactionTransferActivity::class.java))
        }
        receiveFAB!!.setOnClickListener {
            sendFAB!!.visibility = View.INVISIBLE
            receiveFAB!!.visibility = View.INVISIBLE

            navFAB!!.animation = AnimationUtils.loadAnimation(this, R.anim.rotate_0deg)
            navFAB!!.animation.fillAfter = true
            startActivity(Intent(this, TransactionReceiveActivity::class.java))
        }
    }

    private fun restoreSaveInstanceState(savedInstanceState: Bundle?) {
        // Restore the current navigation position.
        savedInstanceState?.also {
            val id = it.getInt(currentNavigation, NavigationPosition.ASSET.id)
            navPosition = findNavigationPositionById(id)
        }
    }

    private fun initNavigation() {
        bottomNavigation = findViewById(R.id.base_main_navigation)
        bottomNavigation!!.disableShiftMode() // Extension function
        bottomNavigation!!.active(navPosition.position)   // Extension function
        bottomNavigation!!.setOnNavigationItemSelectedListener(this)
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
