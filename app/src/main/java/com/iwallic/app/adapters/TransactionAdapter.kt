package com.iwallic.app.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.models.PageData
import com.iwallic.app.models.transactions
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.iwallic.app.pages.transaction.TransactionDetailActivity
import com.iwallic.app.states.TransactionState

class TransactionAdapter(data: PageData<transactions>, recyclerView: RecyclerView): RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {
    private val VIEW_TYPE_CELL = 1
    private val VIEW_TYPE_FOOTER = 0
    private var pageData = data
    private var pagerTV: TextView? = null

    init {
        if (recyclerView.layoutManager is LinearLayoutManager) {
            val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val currPosition = linearLayoutManager.findLastVisibleItemPosition()
                    if (newState == 1 && currPosition == pageData.data.size && pageData.data.size < pageData.total) {
                        Log.i("交易状态", "加载更多")
                        resolveNext()
                    }
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: FrameLayout
        if (viewType == VIEW_TYPE_CELL) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_transaction_list, parent, false) as FrameLayout
        } else {
            view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_pager, parent, false) as FrameLayout
            pagerTV = view.findViewById(R.id.adapter_pager)
            pagerTV?.setText(if (pageData.data.size < pageData.total) R.string.list_loadmore else R.string.list_nomore)
        }
        return ViewHolder(view)
    }

    @Suppress("DEPRECATION")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < pageData.data.size) {
            val txidTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_txid)
            txidTV.text = pageData.data[position].txid
            val valueTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_value)
            valueTV.text = pageData.data[position].value
            val nameTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_name)
            nameTV.text = pageData.data[position].name
            if (pageData.data[position].value.startsWith("-")) {
                txidTV.setTextColor(R.attr.colorDefault)
                nameTV.setTextColor(R.attr.colorDefault)
                valueTV.setTextColor(R.attr.colorDefault)
                holder.itemView.findViewById<ImageView>(R.id.transaction_list_icon).setImageResource(R.drawable.icon_tx_out)
            }
            holder.itemView.setOnClickListener {
                val intent = Intent(it.context, TransactionDetailActivity::class.java)
                intent.putExtra("txid", pageData.data[position].txid)
                it.context.startActivity(intent)
            }
            holder.itemView.setOnLongClickListener {
                val clipboard = it.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("txid", pageData.data[position].txid)
                clipboard.primaryClip = clip
                Toast.makeText(it.context, R.string.error_copied, Toast.LENGTH_SHORT).show()

                val vibratorService = it.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (vibratorService.hasVibrator()) { // Vibrator availability checking
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibratorService.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)) // New vibrate method for API Level 26 or higher
                    } else {
                        vibratorService.vibrate(100) // Vibrate method for below API Level 26
                    }
                }
                return@setOnLongClickListener true
            }
        }
    }

    override fun getItemCount() = pageData.data.size + 1

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == pageData.data.size) VIEW_TYPE_FOOTER else VIEW_TYPE_CELL
    }

    private fun resolveNext() {
        pagerTV?.setText(R.string.list_loading)
        TransactionState.next()
    }

    class ViewHolder(
            listView: FrameLayout
    ): RecyclerView.ViewHolder(listView)
}
