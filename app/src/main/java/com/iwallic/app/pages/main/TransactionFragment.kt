package com.iwallic.app.pages.main

import android.content.*
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.adapters.TransactionAdapter
import com.iwallic.app.base.BaseFragment
import com.iwallic.app.base.BaseFragmentAdapter
import com.iwallic.app.models.PageDataPyModel
import com.iwallic.app.models.Pager
import com.iwallic.app.pages.transaction.TransactionDetailActivity
import com.iwallic.app.pages.transaction.TransactionUnconfirmedActivity
import com.iwallic.app.pages.transaction.TxConfirmedFragment
import com.iwallic.app.pages.transaction.TxUnConfirmedFragment
import com.iwallic.app.states.TransactionState
import com.iwallic.app.utils.CommonUtils
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.WalletUtils
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import io.reactivex.disposables.Disposable

class TransactionFragment : BaseFragment() {

    private lateinit var tabTL: TabLayout
    private lateinit var pagerVP: ViewPager
    private lateinit var adapter: BaseFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)
        initDOM(view)
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
