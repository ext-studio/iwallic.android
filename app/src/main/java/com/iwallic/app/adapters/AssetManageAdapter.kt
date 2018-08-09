package com.iwallic.app.adapters

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.iwallic.app.R
import com.iwallic.app.models.AssetRes
import com.iwallic.app.models.PageDataPyModel
import com.iwallic.app.utils.*

class AssetManageAdapter(_data: PageDataPyModel<AssetRes>, _display: ArrayList<AssetRes>): RecyclerView.Adapter<AssetManageAdapter.ViewHolder>() {
    private var data = _data
    private var display = _display

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_asset_manage, parent, false) as ViewGroup
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.asset_manage_name).text = data.items[position].symbol
        val toggleSC = holder.itemView.findViewById<SwitchCompat>(R.id.asset_manage_toggle)
        if (listOf(CommonUtils.EXT, CommonUtils.EDS, CommonUtils.NEO, CommonUtils.GAS).contains(data.items[position].asset_id)) {
            toggleSC.visibility = View.GONE
        } else {
            toggleSC.visibility = View.VISIBLE
            toggleSC.isChecked = display.indexOfFirst {
                it.asset_id == data.items[position].asset_id
            } >= 0
            toggleSC.setOnClickListener {
                if (toggleSC.isChecked) {
                    SharedPrefUtils.addAsset(holder.itemView.context, data.items[position])
                    Log.i("【AssetManage】", "switch【${data.items[position].symbol}】to【on】")
                } else {
                    SharedPrefUtils.rmAsset(holder.itemView.context, data.items[position].asset_id)
                    Log.i("【AssetManage】", "switch【${data.items[position].symbol}】to【off】")
                }
            }
        }
    }

    override fun getItemCount() = data.items.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun push(newData: PageDataPyModel<AssetRes>) {
        if (newData.page == 1) {
            notifyItemRangeRemoved(0, data.items.size)
            data = newData
            notifyItemRangeInserted(0, data.items.size)
        } else {
            val p = data.items.size
            data.page = newData.page
            data.pages = newData.pages
            data.total = newData.total
            data.per_page = newData.per_page
            data.items.addAll(newData.items)
            notifyItemRangeInserted(p, newData.items.size)
        }
    }

    fun getItem(position: Int): AssetRes {
        return data.items[position]
    }

    class ViewHolder(
        listView: ViewGroup
    ): RecyclerView.ViewHolder(listView)
}