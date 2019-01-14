package com.iwallic.app.pages.transaction

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.View
import com.iwallic.app.R
import com.iwallic.app.base.BaseFragmentAdapter
import com.iwallic.app.base.SwipeBackActivity
import com.iwallic.app.models.Pager

class TransactionActivity : SwipeBackActivity() {
    private lateinit var tabTL: TabLayout
    private lateinit var pagerVP: ViewPager
    private lateinit var adapter: BaseFragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)
        initDOM()
    }

    private fun initDOM() {
        tabTL = findViewById(R.id.tx_tab)
        pagerVP = findViewById(R.id.tx_pager)
        adapter = BaseFragmentAdapter(supportFragmentManager)

        adapter.setPage(Pager(0, TxConfirmedFragment(), resources.getString(R.string.fragment_transaction_title)))
        adapter.setPage(Pager(1, TxUnConfirmedFragment(), resources.getString(R.string.fragment_transaction_unconfirmed)))

        pagerVP.adapter = adapter
        pagerVP.currentItem = 0
        tabTL.setupWithViewPager(pagerVP)
    }
}
