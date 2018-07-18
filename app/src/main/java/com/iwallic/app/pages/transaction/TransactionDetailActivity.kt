package com.iwallic.app.pages.transaction

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.adapters.TransactionDetailAdapter
import com.iwallic.app.utils.HttpClient
import com.iwallic.app.models.TransactionDetailRes

class TransactionDetailActivity : BaseActivity() {
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_detail)

        var txid = this.intent.extras.get("txid")
        findViewById<TextView>(R.id.transaction_detail_txid).setText("${txid}")

        HttpClient.post("gettransferbytxid", listOf("${txid}"), fun (res) {
            Log.e("【交易详情】", "${res}")
            val data = gson.fromJson<ArrayList<TransactionDetailRes>>(res, object: TypeToken<ArrayList<TransactionDetailRes>>() {}.type)
//            Log.e("【data】", "${data}")
            if (data !== null) {
//                findViewById<TextView>(R.id.transaction_detail_txBlockIndex).text = data[0].blockIndex as String
            }
        }, fun(err) {
            Log.e("【接收交易失败】", "${err}")
        })

        HttpClient.post("getnep5transferbytxid", listOf("${txid}"), fun (res) {
            Log.e("【交易详情】", "${res}")
            val data = gson.fromJson<ArrayList<TransactionDetailRes>>(res, object: TypeToken<ArrayList<TransactionDetailRes>>() {}.type)
            if (data !== null) {
//                findViewById<TextView>(R.id.transaction_detail_txBlockIndex).text = data[0].blockIndex as String
            }
        }, fun(err) {
            Log.e("【接收交易失败】", "${err}")
        })

        val backLL = findViewById<LinearLayout>(R.id.transaction_detail__back)
        backLL.setOnClickListener {
            finish()
        }
    }
}
