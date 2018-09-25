package com.iwallic.app.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.models.AssetRes
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class AssetAdapter(list: ArrayList<AssetRes>): RecyclerView.Adapter<AssetAdapter.ViewHolder>() {
    private var data = list
    private var onClickListener: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_asset, parent, false) as FrameLayout

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.asset_list_name).text = data[position].symbol
        holder.itemView.findViewById<TextView>(R.id.asset_list_balance).text = if (data[position].balance == "0.0") "0" else data[position].balance
        holder.itemView.setOnClickListener {
            onClickListener?.invoke(position)
        }
    }

    override fun getItemCount() = data.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setOnAssetClickListener(listener: (Int) -> Unit) {
        onClickListener = listener
    }

    fun set(newData: ArrayList<AssetRes>) {
        data = newData
        notifyDataSetChanged()
    }

    fun getAssetId(position: Int): String {
        return data[position].asset_id
    }

    class ViewHolder(
        listView: FrameLayout
    ): RecyclerView.ViewHolder(listView)
}
