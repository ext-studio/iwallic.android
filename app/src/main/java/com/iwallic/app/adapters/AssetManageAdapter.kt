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

class AssetManageAdapter(_data: ArrayList<AssetRes>, _display: ArrayList<AssetRes>): RecyclerView.Adapter<AssetManageAdapter.ViewHolder>() {
    private var data = _data
    private var display = _display
    private var _onToggle: ((Int, Boolean) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_asset_manage, parent, false) as ViewGroup
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.asset_manage_name).text = data[position].symbol
        val toggleSC = holder.itemView.findViewById<SwitchCompat>(R.id.asset_manage_toggle)
        if (listOf(CommonUtils.EXT, CommonUtils.EDS, CommonUtils.NEO, CommonUtils.GAS).contains(data[position].asset_id)) {
            toggleSC.visibility = View.GONE
        } else {
            toggleSC.visibility = View.VISIBLE
            toggleSC.isChecked = display.indexOfFirst {
                it.asset_id == data[position].asset_id
            } >= 0
            toggleSC.setOnClickListener {
                _onToggle?.invoke(position, toggleSC.isChecked)
            }
        }
    }

    override fun getItemCount() = data.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setOnToggle(listener: (Int, Boolean) -> Unit) {
        _onToggle = listener
    }

    fun getAsset(position: Int): AssetRes {
        return data[position]
    }

    fun push(newData: ArrayList<AssetRes>) {
        val old = data.size
        data.addAll(newData)
        notifyItemRangeInserted(old, newData.size)
    }

    fun set(newData: ArrayList<AssetRes>) {
        data = newData
        notifyDataSetChanged()
    }

    class ViewHolder(
        listView: ViewGroup
    ): RecyclerView.ViewHolder(listView)
}