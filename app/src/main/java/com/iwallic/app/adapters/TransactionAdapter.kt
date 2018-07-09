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

    var data = list
    override fun getCount(): Int {
        return data.size
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        Log.i("交易列表", position.toString())
        Log.i("交易列表数据", data[position].name)
        val inflater = LayoutInflater.from(context)
        val rowView = inflater.inflate(R.layout.adapter_transaction_list, parent, false)
        rowView.findViewById<TextView>(R.id.transaction_list_name).text = data[position].name
        rowView.findViewById<TextView>(R.id.transaction_list_txid).text = data[position].txid
        return rowView
    }

    override fun getItem(position: Int): transactions {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
