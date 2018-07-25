package com.iwallic.app.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.iwallic.app.R
import com.iwallic.app.models.PageDataPyModel
import com.iwallic.app.models.PageDataRes
import com.iwallic.app.models.TransactionRes
import com.iwallic.app.utils.CommonUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class TransactionAdapter(_data: PageDataPyModel<TransactionRes>): RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {
    private var data = _data
    private val _onEnter = PublishSubject.create<Int>()
    private val _onCopy = PublishSubject.create<Int>()
    private val VIEW_TYPE_CELL = 1
    private val VIEW_TYPE_FOOTER = 0
    private var pagerTV: TextView? = null
    private var paging: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: FrameLayout
        if (viewType == VIEW_TYPE_CELL) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_transaction_list, parent, false) as FrameLayout
        } else {
            view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_pager, parent, false) as FrameLayout
            pagerTV = view.findViewById(R.id.adapter_pager)
            pagerTV?.setText(if (data.items.size < data.total) R.string.list_loadmore else R.string.list_nomore)
        }
        return ViewHolder(view)
    }

    @Suppress("DEPRECATION")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == data.items.size) {
            if (pagerTV == null) {
                pagerTV = holder.itemView.findViewById(R.id.adapter_pager)
            }
            return
        }
        val txidTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_txid)
        txidTV.text = data.items[position].txid
        val valueTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_value)
        valueTV.text = data.items[position].value
        val nameTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_name)
        nameTV.text = data.items[position].name
        val statusTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_status)
        if (data.items[position].value.startsWith("-")) {
            val colorOut = CommonUtils.getAttrColor(holder.itemView.context, R.attr.colorFont)
            txidTV.setTextColor(colorOut)
            nameTV.setTextColor(colorOut)
            valueTV.setTextColor(colorOut)
            holder.itemView.findViewById<ImageView>(R.id.transaction_list_icon).setImageResource(R.drawable.icon_tx_out)
        } else {
            val colorIn = CommonUtils.getAttrColor(holder.itemView.context, R.attr.colorDanger)
            txidTV.setTextColor(colorIn)
            nameTV.setTextColor(colorIn)
            valueTV.setTextColor(colorIn)
            holder.itemView.findViewById<ImageView>(R.id.transaction_list_icon).setImageResource(R.drawable.icon_tx_in)
        }
        if (data.items[position].status != "confirmed") {
            statusTV.setText(if (data.items[position].status == "unconfirmed") R.string.adapter_tx_status_un else R.string.adapter_tx_status_no)
            statusTV.visibility = View.VISIBLE
        } else {
            statusTV.visibility = View.GONE
        }
        holder.itemView.setOnLongClickListener {
            _onCopy.onNext(position)
            return@setOnLongClickListener true
        }
        holder.itemView.setOnClickListener {
            _onEnter.onNext(position)
        }
    }

    override fun getItemCount() = data.items.size + 1

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == data.items.size) VIEW_TYPE_FOOTER else VIEW_TYPE_CELL
    }

    fun onEnter(): Observable<Int> {
        return _onEnter
    }

    fun onCopy(): Observable<Int> {
        return _onCopy
    }

    fun push(newData: PageDataPyModel<TransactionRes>) {
        if (newData.page == 1) {
            notifyItemRangeRemoved(0, data.items.size)
            data = newData
            notifyItemRangeInserted(0, data.items.size)
        } else {
            val p = data.items.size
            data.page = newData.page
            data.total = newData.total
            data.per_page = newData.per_page
            data.items.addAll(newData.items)
            notifyItemRangeInserted(p, newData.items.size)
        }
        pagerTV?.setText(if (data.items.size != data.total) R.string.list_loadmore else R.string.list_nomore)
        paging = false
    }

    fun checkNext(position: Int): Boolean {
        if (paging) {
            return false
        }
        return position == data.items.size && data.items.size < data.total
    }

    fun getPage(): Int {
        return data.page
    }

    fun setPaging() {
        if (paging) {
            return
        }
        paging = true
        pagerTV?.setText(R.string.list_loading)
    }

    fun getItem(position: Int): TransactionRes {
        return data.items[position]
    }

    class ViewHolder(
        listView: FrameLayout
    ): RecyclerView.ViewHolder(listView)
}
