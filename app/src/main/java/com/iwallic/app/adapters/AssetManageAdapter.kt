package com.iwallic.app.adapters

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.iwallic.app.R
import com.iwallic.app.models.assetmanage

class AssetManageAdapter(list: ArrayList<assetmanage>): RecyclerView.Adapter<AssetManageAdapter.ViewHolder>() {
    private var data = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_asset_manage, parent, false) as FrameLayout

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.asset_manage_name).text = data[position].symbol
        val toggleSC = holder.itemView.findViewById<SwitchCompat>(R.id.asset_manage_toggle)
        toggleSC.isChecked = true
        toggleSC.setOnCheckedChangeListener { _, isChecked ->
            Log.i("资产管理状态", "【${data[position].symbol}】被设为了【${isChecked}】")
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