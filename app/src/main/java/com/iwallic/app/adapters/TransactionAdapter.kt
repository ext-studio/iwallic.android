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

class TransactionAdapter(_data: ArrayList<TransactionRes>): RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {
    private var data = _data
    private var onEnterListener: ((Int) -> Unit)? = null
    private var onCopyListener: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = if (viewType == 0) {
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_pager, parent, false) as ViewGroup
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_transaction_list, parent, false) as ViewGroup
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (data.size == 0) {
            return
        }
        val txidTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_txid)
        txidTV.text = data[position].txid
        val valueTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_value)
        valueTV.text = data[position].value
        val nameTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_name)
        nameTV.text = data[position].name
        val statusTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_status)
        if (data[position].value.startsWith("-")) {
            val colorOut = CommonUtils.getAttrColor(holder.itemView.context, R.attr.colorGrayL)
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
        if (data[position].status != "confirmed") {
            statusTV.setText(if (data[position].status == "unconfirmed") R.string.adapter_tx_status_un else R.string.adapter_tx_status_no)
            statusTV.visibility = View.VISIBLE
        } else {
            statusTV.visibility = View.GONE
        }
        holder.itemView.setOnLongClickListener {
            onCopyListener?.invoke(position)
            true
        }
        holder.itemView.setOnClickListener {
            onEnterListener?.invoke(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (data.size == 0) 0 else 1
    }

    override fun getItemCount() = if (data.size == 0) 1 else data.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setOnTxEnterListener(listener: (Int) -> Unit) {
        onEnterListener = listener
    }

    fun setOnTxCopyListener(listener: (Int) -> Unit) {
        onCopyListener = listener
    }

    fun set(newData: ArrayList<TransactionRes>) {
        data = newData
        notifyDataSetChanged()
    }

    fun push(newData: ArrayList<TransactionRes>) {
        val oldSize = data.size
        data.addAll(newData)
        notifyItemRangeInserted(oldSize, newData.size)
    }

    fun getItem(position: Int): TransactionRes {
        return data[position]
    }

    class ViewHolder(
        listView: ViewGroup
    ): RecyclerView.ViewHolder(listView)
}
