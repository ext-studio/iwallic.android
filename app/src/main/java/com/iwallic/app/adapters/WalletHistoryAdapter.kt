package com.iwallic.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.iwallic.app.R
import com.iwallic.app.models.WalletAgentModel

class WalletHistoryAdapter(context: Context, _list: ArrayList<WalletAgentModel>): BaseAdapter() {
    private val list = _list
    private val inflater = LayoutInflater.from(context)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder?
        var view: View? = convertView
        if (view == null) {
            holder = ViewHolder()
            view = inflater.inflate(R.layout.adapter_wallet_history, parent, false)
            holder.snapshotTextView = view.findViewById(R.id.adapter_wallet_history_snapshot)
            holder.countTextView = view.findViewById(R.id.adapter_wallet_history_count)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }
        holder.snapshotTextView.text = list[position].snapshot
        holder.countTextView.text = list[position].count.toString()
        return view!!
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(p0: Int): Any {
        return list[p0]
    }

    override fun getItemId(p0: Int): Long {
        return list[p0]._ID
    }

    private class ViewHolder {
        lateinit var snapshotTextView: TextView
        lateinit var countTextView: TextView
    }
}
