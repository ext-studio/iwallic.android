package com.iwallic.app.adapters

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.models.BalanceRes
import com.iwallic.app.pages.asset.AssetDetailActivity

class AssetAdapter(list: ArrayList<BalanceRes>): RecyclerView.Adapter<AssetAdapter.ViewHolder>() {
    private var data = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_asset_list, parent, false) as FrameLayout

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.asset_list_name).text = data[position].symbol
        holder.itemView.findViewById<TextView>(R.id.asset_list_balance).text = data[position].balance
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, AssetDetailActivity::class.java)
            intent.putExtra("asset", data[position].assetId)
            it.context.startActivity(intent)
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
