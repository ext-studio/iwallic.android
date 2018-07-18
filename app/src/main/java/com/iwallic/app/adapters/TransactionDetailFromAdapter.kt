package com.iwallic.app.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.iwallic.app.R
import android.util.Log
import com.iwallic.app.models.TransactionDetailRes

class TransactionDetailFromAdapter(list: ArrayList<TransactionDetailRes>): RecyclerView.Adapter<TransactionDetailFromAdapter.ViewHolder>() {
    private var data = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_transaction_detail_from, parent, false)

        return ViewHolder(view as FrameLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.transaction_detail_txFromAddr).text = data[position].from
        holder.itemView.findViewById<TextView>(R.id.transaction_detail_txFromName).text = data[position].name
        holder.itemView.findViewById<TextView>(R.id.transaction_detail_txFromValue).text = data[position].value.toString()
    }

    override fun getItemCount() = data.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolder(
            listView: FrameLayout
    ): RecyclerView.ViewHolder(listView)
}