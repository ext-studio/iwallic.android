package com.iwallic.app.pages.main

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iwallic.app.R
import com.iwallic.app.base.BaseFragment
import com.iwallic.app.base.BaseFragmentAdapter
import com.iwallic.app.base.BaseLazyFragment
import com.iwallic.app.models.Pager
import com.iwallic.app.pages.transaction.TxConfirmedFragment
import com.iwallic.app.pages.transaction.TxUnConfirmedFragment

class TransactionFragment : BaseLazyFragment() {

    private lateinit var tabTL: TabLayout
    private lateinit var pagerVP: ViewPager
    private lateinit var adapter: BaseFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)
        initDOM(view)
        notifyCreatedView()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun initDOM(view: View) {
        tabTL = view.findViewById(R.id.tx_tab)
        pagerVP = view.findViewById(R.id.tx_pager)
        adapter = BaseFragmentAdapter(childFragmentManager)

        adapter.setPage(Pager(0, TxConfirmedFragment(), resources.getString(R.string.fragment_transaction_title)))
        adapter.setPage(Pager(1, TxUnConfirmedFragment(), resources.getString(R.string.fragment_transaction_unconfirmed)))

        pagerVP.adapter = adapter
        pagerVP.currentItem = 0
        tabTL.setupWithViewPager(pagerVP)
    }
}
