package com.iwallic.app.pages.transaction

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
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
import kotlin.collections.ArrayList

class TransactionDetailActivity : BaseActivity() {
    private val gson = Gson()
    private lateinit var txid: Any
    private lateinit var TransactionDetailFromRV: RecyclerView
    private lateinit var TransactionDetailToRV: RecyclerView
    private lateinit var TransactionDetailFromManager: RecyclerView.LayoutManager
    private lateinit var TransactionDetailToManager: RecyclerView.LayoutManager
    private lateinit var transactionDetailFromAdapter: RecyclerView.Adapter<*>
    private lateinit var transactionDetailToAdapter: RecyclerView.Adapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_detail)

        txid = this.intent.extras.get("txid")
        findViewById<TextView>(R.id.transaction_detail_txid).setText("${txid}")

        getnep5transferbytxid()
        gettransferbytxid()

        val backLL = findViewById<LinearLayout>(R.id.transaction_detail_back)
        backLL.setOnClickListener {
            finish()
        }
    }

    fun getnep5transferbytxid() {
        HttpClient.post("getnep5transferbytxid", listOf("${txid}"), fun (res) {
            Log.e("【交易详情】", "${res}")
            val data = gson.fromJson<ArrayList<TransactionDetailRes>>(res, object: TypeToken<ArrayList<TransactionDetailRes>>() {}.type)
            if (data !== null) {
                val format: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val date = format.format(data[0].time.toDouble() * 1000)
                findViewById<TextView>(R.id.transaction_detail_txTime).text = date
                findViewById<TextView>(R.id.transaction_detail_txBlockIndex).text = data[0].blockIndex.toString()

                resolveData(data)
            }
        }, fun(err) {
            Log.e("【接收交易失败】", "${err}")
        })

    }
    fun gettransferbytxid() {
        HttpClient.post("gettransferbytxid", listOf("${txid}"), fun (res) {
            Log.e("【交易详情】", "${res}")
//            val data = gson.fromJson<ArrayList<TransactionDetailRes>>(res, object: TypeToken<ArrayList<TransactionDetailRes>>() {}.type)
//            if (data !== null) {
//
//            }
        }, fun(err) {
            Log.e("【接收交易失败】", "${err}")
        })
    }
    fun resolveData(data: ArrayList<TransactionDetailRes>) {
        var fromCount = 0
        var toCount = 0
        var fromData: ArrayList<TransactionDetailRes> = ArrayList()
        var toData: ArrayList<TransactionDetailRes> = ArrayList()
        for (index in data) {
            if (index.from != "") {
                fromCount++
                fromData.add(index)
            }
            if (index.to != "") {
                toCount++
                toData.add(index)
            }
        }
        if (fromCount == 0) {
            findViewById<TextView>(R.id.transaction_detail_from).visibility = View.INVISIBLE
        } else {
            resolveFromList(fromData)
        }
        if (toCount == 0) {
            findViewById<TextView>(R.id.transaction_detail_to).visibility = View.INVISIBLE
        } else {
            resolveToList(toData)
        }
    }
    fun resolveFromList(data: ArrayList<TransactionDetailRes>) {
        transactionDetailFromAdapter = TransactionDetailFromAdapter(data)
        TransactionDetailFromRV = findViewById(R.id.transaction_detail_list_from)

        TransactionDetailFromManager = LinearLayoutManager(this)
        TransactionDetailFromRV.apply {
            setHasFixedSize(true)
            layoutManager = TransactionDetailFromManager
            adapter = transactionDetailFromAdapter
        }
    }
    fun resolveToList(data: ArrayList<TransactionDetailRes>) {
        transactionDetailToAdapter = TransactionDetailToAdapter(data)
        TransactionDetailToRV = findViewById(R.id.transaction_detail_list_to)

        TransactionDetailToManager = LinearLayoutManager(this)

        TransactionDetailToRV.apply {
            setHasFixedSize(true)
            layoutManager = TransactionDetailToManager
            adapter = transactionDetailToAdapter
        }
    }
}

