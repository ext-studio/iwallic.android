package com.iwallic.app.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.iwallic.app.R
import com.iwallic.app.models.transactions

class TransactionAdapter(context: Context, layout: Int, list: ArrayList<transactions>): ArrayAdapter<transactions>(context, layout, list) {

    private var data = list
    private val inflater = LayoutInflater.from(context)
    override fun getCount(): Int {
        return data.size
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: TransactionViewHolder?
        var view: View? = convertView
        if (view == null) {
            holder = TransactionViewHolder()
            view = inflater.inflate(R.layout.adapter_transaction_list, parent, false)
            holder.nameTextView = view.findViewById(R.id.transaction_list_name)
            holder.valueTextView = view.findViewById(R.id.transaction_list_value)
            holder.txidTextView = view.findViewById(R.id.transaction_list_txid)
            view.tag = holder
        } else {
            holder = view.tag as TransactionViewHolder
        }
        holder.nameTextView.text = data[position].txid
        holder.valueTextView.text = data[position].value
        holder.nameTextView.text = data[position].name
        return view!!
    }

    override fun getItem(position: Int): transactions {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}

private class TransactionViewHolder {
    lateinit var nameTextView: TextView
    lateinit var valueTextView: TextView
    lateinit var txidTextView: TextView
}

