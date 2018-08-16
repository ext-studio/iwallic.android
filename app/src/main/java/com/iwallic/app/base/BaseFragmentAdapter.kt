package com.iwallic.app.base

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class BaseFragmentAdapter(manager: FragmentManager): FragmentStatePagerAdapter(manager) {
    private val pages = arrayListOf<BaseFragment>()
    private val titles = arrayListOf<String>()
    override fun getItem(position: Int): BaseFragment {
        return pages[position]
    }

    override fun getCount(): Int {
        return pages.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }

    fun setPage(page: BaseFragment, title: String = "") {
        pages.add(page)
        titles.add(title)
    }
}
