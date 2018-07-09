package com.iwallic.app.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.iwallic.app.R
import com.iwallic.app.models.addrassets

class AssetAdapter(context: Context, layout: Int, list: ArrayList<addrassets>): ArrayAdapter<addrassets>(context, layout, list) {

    private var data = list
    private val inflater = LayoutInflater.from(context)
    override fun getCount(): Int {
        return data.size
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var holder: ViewHolder? = null
        var view: View? = convertView
        if (view == null) {
            holder = ViewHolder()
            view = inflater.inflate(R.layout.adapter_asset_list, parent, false)
            holder.nameTextView = view.findViewById(R.id.asset_list_name)
            holder.balanceTextView = view.findViewById(R.id.asset_list_balance)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }
        holder.nameTextView.text = data[position].name
        holder.balanceTextView.text = data[position].balance
        return view!!
    }

    override fun getItem(position: Int): addrassets {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}

private class ViewHolder {
    lateinit var nameTextView: TextView
    lateinit var balanceTextView: TextView
}
