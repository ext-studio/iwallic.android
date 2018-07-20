package com.iwallic.app.pages.transaction

import android.os.Bundle
import android.util.Log
import android.view.Gravity.CENTER
import android.view.Gravity.RIGHT
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.LinearLayout.VERTICAL
import android.widget.ProgressBar
import android.widget.TextView
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import com.iwallic.app.models.*
import com.iwallic.app.utils.HttpClient
import kotlin.collections.ArrayList
import com.iwallic.app.models.TransactionDetailFromRes
import com.iwallic.app.models.TransactionDetailToRes
import java.util.*

class TransactionDetailActivity : BaseActivity() {
    private lateinit var txid: Any
    private lateinit var loadPB: ProgressBar
    private var fromFlag: Boolean = true
    private var toFlag: Boolean = true
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_detail)

        loadPB = findViewById(R.id.transaction_detail_load)

        txid = this.intent.extras.get("txid")
        findViewById<TextView>(R.id.transaction_detail_txid).text = txid.toString()

        getnep5transferbytxid()

        val backLL = findViewById<LinearLayout>(R.id.transaction_detail_back)
        backLL.setOnClickListener {
            finish()
        }
    }

    private fun getnep5transferbytxid() {  // from and to
        HttpClient.post("getnep5transferbytxid", listOf(txid), fun (nep5Res) {
            Log.i("【nep5交易详情】", "result 【${nep5Res}】")
            if (nep5Res != "") {
                val data = gson.fromJson<ArrayList<TransactionDetailRes>>(nep5Res, object : TypeToken<ArrayList<TransactionDetailRes>>() {}.type)
                if (data.size > 0) {
                    val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(data[0].time * 1000)
                    findViewById<TextView>(R.id.transaction_detail_txTime).text = time
                    findViewById<TextView>(R.id.transaction_detail_txBlockIndex).text = data[0].blockIndex.toString()

                    resolveData(data, false, "from")
                    resolveData(data, false, "to")
                }
            }
            gettransferbytxid()
        }, fun(err) {
            Log.i("【接收nep5交易详情失败】", "error 【${err}】")
        })
    }

    private fun gettransferbytxid() {  // address
        HttpClient.post("gettransferbytxid", listOf(txid), fun (res) {
            Log.i("【交易详情】", "result 【${res}】")
            if (res != "") {
                val fromDataTemp = gson.fromJson<TransactionDetailFromRes>(res, object: TypeToken<TransactionDetailFromRes>() {}.type)
                val toDataTemp = gson.fromJson<TransactionDetailToRes>(res, object : TypeToken<TransactionDetailToRes>() {}.type)

                if (fromDataTemp.TxUTXO.size > 0) resolveData(fromDataTemp.TxUTXO, true, "from")
                if (toDataTemp.TxVouts.size > 0) resolveData(toDataTemp.TxVouts, true, "to")
            }

            // render page
            if (fromFlag) {
                findViewById<TextView>(R.id.transaction_detail_from).visibility = View.GONE
            }
            if (toFlag) {
                findViewById<TextView>(R.id.transaction_detail_to).visibility = View.GONE
            }

            if (loadPB.visibility == View.VISIBLE) {
                loadPB.visibility = View.GONE
            }
        }, fun(err) {
            Log.i("【接收交易详情失败】", "error 【${err}】")
        })
    }

    private fun resolveData(data: ArrayList<TransactionDetailRes>, flag: Boolean, direction: String) {
        var count = 0
        for (index in data) {
            if (flag) {  // not Nep5 transfer
                if (index.address != "") {
                    count++
                    if (direction == "from") {
                        addTransferView(index, "from")
                    } else {
                        addTransferView(index, "to")
                    }
                }
            } else { // Nep5 transfer
                if (direction == "from") {
                    if (index.from != "") {
                        count++
                        index.address = index.from
                        addTransferView(index, "from")
                    }
                } else {
                    if (index.to != "") {
                        count++
                        index.address = index.to
                        addTransferView(index, "to")
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

    private fun addTransferView(data: TransactionDetailRes, direction: String) {
        lateinit var linearLayout: LinearLayout
        when (direction) {
            "from" -> {
                linearLayout = findViewById(R.id.transaction_detail_from_list)
            }
            "to" -> {
                linearLayout = findViewById(R.id.transaction_detail_to_list)
            }
        }
        val row = LinearLayout(this)
        val address = TextView(this)
        val line = LinearLayout(this)
        val value = TextView(this)
        val name = TextView(this)
        val rowParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        val colParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 0.5F)
        row.orientation = VERTICAL
        row.gravity = CENTER
        row.layoutParams = rowParams

        address.gravity = CENTER
        address.text = data.address
        address.layoutParams = rowParams
        address.setPadding(0, 8, 0, 8)

        line.orientation = HORIZONTAL
        line.setPadding(0, 8, 0, 8)
        line.weightSum = 1F
        line.layoutParams = rowParams

        value.text = data.value.toString()
        value.setPadding(0, 0, 12, 0)
        value.layoutParams = colParams
        value.gravity  = RIGHT

        name.text = data.name
        name.layoutParams = colParams
        name.gravity = LEFT
        name.setPadding(12, 0, 0, 0)

        line.addView(value)
        line.addView(name)
        row.addView(address)
        row.addView(line)
        linearLayout.addView(row)
    }
}

