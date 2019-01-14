package com.iwallic.app.base

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.iwallic.app.models.Pager

class BaseFragmentAdapter(manager: FragmentManager): FragmentStatePagerAdapter(manager) {
    private var pages = arrayListOf<Pager>()
    override fun getItem(position: Int): BaseFragment {
        return pages[position].fragment
    }

    override fun getCount(): Int {
        return pages.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return pages[position].title
    }

    fun setPage(page: Pager): Int {
        pages.add(page)
        notifyDataSetChanged()
        return pages.size
    }
    fun setPages(_pages: ArrayList<Pager>): Int {
        pages = _pages
        notifyDataSetChanged()
        return pages.size
    }
    fun getPosition(id: Int): Int {
        return pages.indexOfFirst {it.id == id}
    }
    fun clear() {
        pages.removeAll { true }
        notifyDataSetChanged()
    }
}
