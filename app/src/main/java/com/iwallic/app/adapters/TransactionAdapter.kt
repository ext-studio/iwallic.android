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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = if (viewType == 0) {
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_pager, parent, false) as ViewGroup
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_transaction_list, parent, false) as ViewGroup
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (data.items.size == 0) {
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

    override fun getItemViewType(position: Int): Int {
        return if (data.items.size == 0) 0 else 1
    }

    override fun getItemCount() = if (data.items.size == 0) 1 else data.items.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
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
    }

    fun getItem(position: Int): TransactionRes {
        return data.items[position]
    }

    class ViewHolder(
        listView: ViewGroup
    ): RecyclerView.ViewHolder(listView)
}
