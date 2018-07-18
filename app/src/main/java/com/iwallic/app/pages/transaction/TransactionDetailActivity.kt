package com.iwallic.app.pages.transaction

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.utils.HttpClient
import com.iwallic.app.models.TransactionDetailRes
import java.text.SimpleDateFormat
import java.util.*
import com.iwallic.app.adapters.TransactionDetailFromAdapter
import com.iwallic.app.adapters.TransactionDetailToAdapter

class TransactionDetailActivity : BaseActivity() {
    private val gson = Gson()
    private lateinit var TransactionDetailFromRV: RecyclerView
    private lateinit var TransactionDetailToRV: RecyclerView
    private lateinit var TransactionDetailFromManager: RecyclerView.LayoutManager
    private lateinit var TransactionDetailToManager: RecyclerView.LayoutManager
    private lateinit var transactionDetailFromAdapter: RecyclerView.Adapter<*>
    private lateinit var transactionDetailToAdapter: RecyclerView.Adapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_detail)

        TransactionDetailFromRV = findViewById(R.id.transaction_detail_list_from)
        TransactionDetailToRV = findViewById(R.id.transaction_detail_list_to)

        var txid = this.intent.extras.get("txid")
        findViewById<TextView>(R.id.transaction_detail_txid).setText("${txid}")

        HttpClient.post("gettransferbytxid", listOf("${txid}"), fun (res) {
            Log.e("【交易详情】", "${res}")
            val data = gson.fromJson<ArrayList<TransactionDetailRes>>(res, object: TypeToken<ArrayList<TransactionDetailRes>>() {}.type)
            if (data !== null) {
                findViewById<TextView>(R.id.transaction_detail_txFromName).text = data[0].name
                findViewById<TextView>(R.id.transaction_detail_txToName).text = data[0].name
            }
        }, fun(err) {
            Log.e("【接收交易失败】", "${err}")
        })

        HttpClient.post("getnep5transferbytxid", listOf("${txid}"), fun (res) {
            Log.e("【交易详情】", "${res}")
            val data = gson.fromJson<ArrayList<TransactionDetailRes>>(res, object: TypeToken<ArrayList<TransactionDetailRes>>() {}.type)
            if (data !== null) {
                val format: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                var date = format.format(data[0].time.toDouble() * 1000)
                findViewById<TextView>(R.id.transaction_detail_txTime).text = date
                findViewById<TextView>(R.id.transaction_detail_txBlockIndex).text = data[0].blockIndex.toString()

                transactionDetailFromAdapter = TransactionDetailFromAdapter(data)
                TransactionDetailFromManager = LinearLayoutManager(this)
                TransactionDetailToManager = LinearLayoutManager(this)
                TransactionDetailFromRV.apply {
                    setHasFixedSize(true)
                    layoutManager = TransactionDetailFromManager
                    adapter = transactionDetailFromAdapter
                }
                transactionDetailToAdapter = TransactionDetailToAdapter(data)
                TransactionDetailToRV.apply {
                    setHasFixedSize(true)
                    layoutManager = TransactionDetailToManager
                    adapter = transactionDetailToAdapter
                }
            }
        }, fun(err) {
            Log.e("【接收交易失败】", "${err}")
        })

        val backLL = findViewById<LinearLayout>(R.id.transaction_detail_back)
        backLL.setOnClickListener {
            finish()
        }
    }
}

