package com.iwallic.app.pages.transaction

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.iwallic.app.utils.HttpClient

class TransactionDetailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_detail)

        var txid = this.intent.extras.get("txid")
        findViewById<TextView>(R.id.transaction_detail_txid).setText("${txid}")

        HttpClient.post("gettransferbytxid", listOf("${txid}"), fun (res) {
            Log.e("【交易详情】", "${res}")
        }, fun(err) {
            Log.e("【接收交易失败】", "${err}")
        })

        HttpClient.post("getnep5transferbytxid", listOf("${txid}"), fun (res) {
            Log.e("【交易详情】", "${res}")
        }, fun(err) {
            Log.e("【接收交易失败】", "${err}")
        })

        val backLL = findViewById<LinearLayout>(R.id.transaction_detail__back)
        backLL.setOnClickListener {
            finish()
        }
    }
}
