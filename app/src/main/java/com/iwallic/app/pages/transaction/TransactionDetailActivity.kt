package com.iwallic.app.pages.transaction

import android.os.Bundle
import android.util.Log
import android.view.Gravity.CENTER
import android.view.View
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
import com.iwallic.app.utils.HttpClient.post
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
        gettransferbytxid()

        val backLL = findViewById<LinearLayout>(R.id.transaction_detail_back)
        backLL.setOnClickListener {
            finish()
        }
    }

    private fun getnep5transferbytxid() {  // from and to
        post("getnep5transferbytxid", listOf(txid), fun (res) {
            Log.i("【nep5交易详情】", "result 【${res}】")
            val data = gson.fromJson<ArrayList<TransactionDetailRes>>(res, object: TypeToken<ArrayList<TransactionDetailRes>>() {}.type)
            if (data !== null) {
                val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(data[0].time * 1000)
                findViewById<TextView>(R.id.transaction_detail_txTime).text = time
                findViewById<TextView>(R.id.transaction_detail_txBlockIndex).text = data[0].blockIndex.toString()

                resolveData(data, false, "from")
                resolveData(data, false, "to")
            }
        }, fun(err) {
            Log.i("【接收nep5交易详情失败】", "error 【${err}】")
        })
    }

    private fun gettransferbytxid() {  // address
        post("gettransferbytxid", listOf(txid), fun (res) {
            Log.i("【交易详情】", "result 【${res}】")
            val fromDatatemp = gson.fromJson<TransactionDetailFromRes<ArrayList<TransactionDetailRes>>>(res, object: TypeToken<TransactionDetailFromRes<ArrayList<TransactionDetailRes>>>() {}.type)
            val toDatatemp = gson.fromJson<TransactionDetailToRes<ArrayList<TransactionDetailRes>>>(res, object : TypeToken<TransactionDetailToRes<ArrayList<TransactionDetailRes>>>() {}.type)

            resolveData(fromDatatemp.TxUTXO, true, "from")
            resolveData(toDatatemp.TxVouts, true, "to")

            // render page
            if (fromFlag) {
                findViewById<TextView>(R.id.transaction_detail_from).visibility = View.INVISIBLE
            }
            if (toFlag) {
                findViewById<TextView>(R.id.transaction_detail_to).visibility = View.INVISIBLE
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
        lateinit var LinearLayout: LinearLayout
        if (direction == "from") {
             LinearLayout = findViewById<LinearLayout>(R.id.transaction_detail_from_list)
        } else {
            LinearLayout = findViewById<LinearLayout>(R.id.transaction_detail_to_list)
        }
        val row = LinearLayout(this)
        row.orientation = VERTICAL
        row.gravity = CENTER
        val address = TextView(this)
        address.gravity = CENTER
        address.text = data.address
        val line = LinearLayout(this)
        line.orientation = HORIZONTAL
        val value = TextView(this)
        value.gravity  = RIGHT
        value.text = data.value.toString()
        val name = TextView(this)
        name.gravity = LEFT
        name.text = data.name

        line.addView(value)
        line.addView(name)
        row.addView(address)
        row.addView(line)
        LinearLayout.addView(row)
    }
}

