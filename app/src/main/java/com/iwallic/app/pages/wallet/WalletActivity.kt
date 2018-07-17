package com.iwallic.app.pages.wallet

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import com.iwallic.app.R
import com.iwallic.app.adapters.WalletHistoryAdapter
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.models.WalletAgentModel
import com.iwallic.app.utils.WalletDBUtils

class WalletActivity : BaseActivity() {
    private lateinit var createB: Button
    private lateinit var importB: Button
    private lateinit var historyFAB: FloatingActionButton
    private lateinit var history: ArrayList<WalletAgentModel>
    private lateinit var historyLV: ListView
    private lateinit var gateLL: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_wallet)
        initDOM()
        initListener()
        initHistory()
    }

    private fun initDOM() {
        createB = findViewById(R.id.wallet_create_btn)
        importB = findViewById(R.id.wallet_import_btn)
        historyFAB = findViewById(R.id.wallet_history_btn)
        historyLV = findViewById(R.id.wallet_history_list)
        gateLL = findViewById(R.id.wallet_gate)
    }

    private fun initListener() {
        createB.setOnClickListener {
            startActivity(Intent(this, WalletCreateActivity::class.java))
        }

        importB.setOnClickListener {
            startActivity(Intent(this, WalletImportActivity::class.java))
        }
        historyFAB.setOnClickListener {
            gateLL.visibility = View.GONE
            historyLV.visibility = View.VISIBLE
        }
    }

    private fun initHistory() {
        history = WalletDBUtils(this).getAll()
        if (history.isNotEmpty()) {
            historyFAB.visibility = View.VISIBLE
            resolveList()
        }
    }

    private fun resolveList() {
        historyLV.adapter = WalletHistoryAdapter(this, history)
        historyLV.setOnItemClickListener { adapterView, view, i, l ->
            Log.i("【Wallet】", "tapped【$i】")
        }
    }
}
