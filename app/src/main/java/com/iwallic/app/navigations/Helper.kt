package com.iwallic.app.navigations

import android.annotation.SuppressLint
import android.support.v4.app.Fragment
import com.iwallic.app.R
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.util.Log

enum class NavigationPosition(val position: Int, val id: Int) {
    ASSET(0, R.id.asset),
    TRANSACTION(1, R.id.transaction),
    FIND(2, R.id.find),
    USER(3, R.id.user)
}

fun findNavigationPositionById(id: Int): NavigationPosition = when (id) {
    NavigationPosition.TRANSACTION.id -> NavigationPosition.TRANSACTION
    NavigationPosition.FIND.id -> NavigationPosition.FIND
    NavigationPosition.USER.id -> NavigationPosition.USER
    else -> NavigationPosition.ASSET
}

fun NavigationPosition.createFragment(): Fragment = when (this) {
    NavigationPosition.ASSET -> AssetFragment.newInstance()
    NavigationPosition.TRANSACTION -> TransactionFragment.newInstance()
    NavigationPosition.FIND -> FindFragment.newInstance()
    NavigationPosition.USER -> UserFragment.newInstance()
}

fun NavigationPosition.getTag(): String = when (this) {
    NavigationPosition.ASSET -> AssetFragment.TAG
    NavigationPosition.TRANSACTION -> TransactionFragment.TAG
    NavigationPosition.FIND -> FindFragment.TAG
    NavigationPosition.USER -> UserFragment.TAG
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

fun BottomNavigationView.active(position: Int) {
    menu.getItem(position).isChecked = true
}
