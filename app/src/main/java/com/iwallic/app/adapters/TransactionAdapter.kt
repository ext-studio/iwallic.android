package com.iwallic.app.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.iwallic.app.R
import com.iwallic.app.models.transactions

class TransactionAdapter(list: ArrayList<transactions>): RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {
    private var data = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_transaction_list, parent, false) as FrameLayout

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.transaction_list_txid).text = data[position].txid
        holder.itemView.findViewById<TextView>(R.id.transaction_list_value).text = data[position].value
        holder.itemView.findViewById<TextView>(R.id.transaction_list_name).text = data[position].name
        holder.itemView.setOnClickListener {
            // todo
        }
    }

    override fun getItemCount() = data.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolder(
            listView: FrameLayout
    ): RecyclerView.ViewHolder(listView)
}
