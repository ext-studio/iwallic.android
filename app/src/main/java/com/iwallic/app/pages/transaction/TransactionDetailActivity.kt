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
import java.text.SimpleDateFormat
import com.iwallic.app.models.*
import com.iwallic.app.utils.HttpClient.post
import kotlin.collections.ArrayList
import com.iwallic.app.models.TransactionDetailFromRes
import com.iwallic.app.models.TransactionDetailToRes
import com.iwallic.app.adapters.TransactionDetailAdapter

class TransactionDetailActivity : BaseActivity() {
    private val gson = Gson()
    private lateinit var txid: Any
    private var fromFlag: Boolean = true
    private var toFlag: Boolean = true
    private var fromData: ArrayList<TransactionDetailRes> = ArrayList()
    private var toData: ArrayList<TransactionDetailRes> = ArrayList()
    private lateinit var TransactionDetailRV: RecyclerView
    private lateinit var TransactionDetailManager: RecyclerView.LayoutManager
    private lateinit var transactionDetailAdapter: RecyclerView.Adapter<*>

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

    fun getnep5transferbytxid() {  // from and to
        post("getnep5transferbytxid", listOf("${txid}"), fun (res) {
            Log.e("【交易详情】", "${res}")
            val data = gson.fromJson<ArrayList<TransactionDetailRes>>(res, object: TypeToken<ArrayList<TransactionDetailRes>>() {}.type)
            if (data !== null) {
                val format: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val date = format.format(data[0].time.toDouble() * 1000)
                findViewById<TextView>(R.id.transaction_detail_txTime).text = date
                findViewById<TextView>(R.id.transaction_detail_txBlockIndex).text = data[0].blockIndex.toString()

                resolveData(data, false, "from")
                resolveData(data, false, "to")
            }
        }, fun(err) {
            Log.e("【接收交易失败】", "${err}")
        })
    }

    fun gettransferbytxid() {  // address
        post("gettransferbytxid", listOf("${txid}"), fun (res) {
            Log.e("【交易详情】", "${res}")
            val fromDatatemp = gson.fromJson<TransactionDetailFromRes<ArrayList<TransactionDetailRes>>>(res, object: TypeToken<TransactionDetailFromRes<ArrayList<TransactionDetailRes>>>() {}.type)
            val toDatatemp = gson.fromJson<TransactionDetailToRes<ArrayList<TransactionDetailRes>>>(res, object : TypeToken<TransactionDetailToRes<ArrayList<TransactionDetailRes>>>() {}.type)

            resolveData(fromDatatemp.TxUTXO, true, "from")
            resolveData(toDatatemp.TxVouts, true, "to")

            resolveList(fromData, "from")
            resolveList(toData, "to")

            if (fromFlag) {
                findViewById<TextView>(R.id.transaction_detail_from).visibility = View.INVISIBLE
            }
            if (toFlag) {
                findViewById<TextView>(R.id.transaction_detail_to).visibility = View.INVISIBLE
            }
        }, fun(err) {
            Log.e("【接收交易失败】", "${err}")
        })
    }
    fun resolveData(data: ArrayList<TransactionDetailRes>, flag: Boolean, direction: String) {
        var count = 0
        for (index in data) {
            if (flag) {  // not Nep5 transfer
                if (index.address != "") {
                    count++
                    if (direction == "from") {
                        fromData.add(index)
                    } else {
                        toData.add(index)
                    }
                }
            } else { // Nep5 transfer
                if (direction == "from") {
                    if (index.from != "") {
                        count++
                        index.address = index.from
                        fromData.add(index)
                    }
                } else {
                    if (index.to != "") {
                        count++
                        index.address = index.to
                        toData.add(index)
                    }
                }
            }
        }
        if (count != 0) {
            if (direction == "from") {
                fromFlag = false

            } else {
                toFlag = false
            }
        }
    }

    fun resolveList(data: ArrayList<TransactionDetailRes>, direction: String) {
        transactionDetailAdapter = TransactionDetailAdapter(data)
        if (direction == "from") {
            TransactionDetailRV = findViewById(R.id.transaction_detail_list_to)
        } else {
            TransactionDetailRV = findViewById(R.id.transaction_detail_list_from)
        }
        TransactionDetailManager = LinearLayoutManager(this)

        TransactionDetailRV.apply {
            setHasFixedSize(true)
            layoutManager = TransactionDetailManager
            adapter = transactionDetailAdapter
        }
    }
}

