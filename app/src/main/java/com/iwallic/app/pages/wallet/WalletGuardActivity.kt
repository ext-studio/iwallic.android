package com.iwallic.app.pages.wallet

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.adapters.WalletHistoryAdapter
import com.iwallic.app.base.BaseAuthActivity
import com.iwallic.app.pages.main.MainActivity
import com.iwallic.app.models.WalletAgentModel
import com.iwallic.app.utils.CommonUtils
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.WalletDBUtils
import com.iwallic.app.utils.NeonUtils

class WalletGuardActivity : BaseAuthActivity() {
    private lateinit var createFL: FrameLayout
    private lateinit var importFL: FrameLayout
    private lateinit var historyFAB: FloatingActionButton
    private lateinit var history: ArrayList<WalletAgentModel>
    private lateinit var historyLL: LinearLayout
    private lateinit var historyRV: RecyclerView
    private lateinit var gateLL: LinearLayout
    private lateinit var backFAB: FloatingActionButton
    private lateinit var adapter: WalletHistoryAdapter

    private lateinit var adapterViewManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_guard)
        initDOM()
        initListener()
        initHistory()
    }

    private fun initDOM() {
        createFL = findViewById(R.id.wallet_create_btn)
        importFL = findViewById(R.id.wallet_import_btn)
        historyFAB = findViewById(R.id.wallet_history_btn)
        historyRV = findViewById(R.id.wallet_history_list_view)
        historyLL = findViewById(R.id.wallet_history_list)
        gateLL = findViewById(R.id.wallet_gate)
        backFAB = findViewById(R.id.wallet_history_list_back)
    }

    private fun initListener() {
        createFL.setOnClickListener {
            startActivity(Intent(this, WalletCreateActivity::class.java))
        }
        importFL.setOnClickListener {
            startActivity(Intent(this, WalletImportActivity::class.java))
        }
        historyFAB.setOnClickListener {
            historyLL.visibility = View.VISIBLE
            gateLL.visibility = View.GONE
        }
        backFAB.setOnClickListener {
            historyLL.visibility = View.GONE
            gateLL.visibility = View.VISIBLE
        }
    }

    private fun initHistory() {
        history = WalletDBUtils(this).getAll()
        if (history.isNotEmpty()) {
            historyFAB.show()
            resolveList()
        } else {
            historyFAB.hide()
        }
    }

    private fun resolveList() {
        adapterViewManager = LinearLayoutManager(this)
        adapter = WalletHistoryAdapter(history)
        historyRV.layoutManager = adapterViewManager
        historyRV.adapter = adapter
        adapter.setOnChooseListener {
            resolveOpen(adapter.getData(it))
        }
        adapter.setOnDeleteListener {
            resolveDel(adapter.getData(it), it)
        }
    }

    private fun resolveOpen(w: WalletAgentModel) {
        CommonUtils.log("open wallet【${w._ID}】")
        DialogUtils.password(this) { pwd ->
            if (pwd.isEmpty()) {
                return@password
            }
            val loader = DialogUtils.loader(this, R.string.transaction_transfer_verifying)
            NeonUtils.switch(this, w, pwd).subscribe ({ rs ->
                loader.dismiss()
                if (rs == 0) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else {
                    if (!DialogUtils.error(this, rs)) {
                        DialogUtils.toast(this, "$rs")
                    }
                }
            }, {
                loader.dismiss()
                DialogUtils.error(this, 99999)
            })
        }
    }

    private fun resolveDel(w: WalletAgentModel, p: Int) {
        CommonUtils.log("del wallet at【$p】")
        DialogUtils.confirm(this, {
            if (!it) {
                return@confirm
            }
            NeonUtils.remove(applicationContext, w)
            adapter.remove(p)
            if (adapter.itemCount == 0) {
                historyLL.visibility = View.GONE
                gateLL.visibility = View.VISIBLE
                historyFAB.hide()
            }
        }, R.string.dialog_content_addrdel, R.string.dialog_title_warn, R.string.dialog_ok, R.string.dialog_no)
    }
}
