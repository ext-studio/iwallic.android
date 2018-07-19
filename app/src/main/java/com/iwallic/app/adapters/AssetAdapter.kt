package com.iwallic.app.adapters

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.models.BalanceRes
import com.iwallic.app.pages.asset.AssetDetailActivity
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class AssetAdapter(list: ArrayList<BalanceRes>): RecyclerView.Adapter<AssetAdapter.ViewHolder>() {
    private var data = list
    private val _onClick = PublishSubject.create<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_asset_list, parent, false) as FrameLayout

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.asset_list_name).text = data[position].symbol
        holder.itemView.findViewById<TextView>(R.id.asset_list_balance).text = data[position].balance
        holder.itemView.setOnClickListener {
            _onClick.onNext(position)
        }
    }

    override fun getItemCount() = data.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun onClick(): Observable<Int> {
        return _onClick
    }

    fun set(newData: ArrayList<BalanceRes>) {
        data = newData
        notifyDataSetChanged()
    }

    fun getAssetId(position: Int): String {
        return data[position].assetId
    }

    class ViewHolder(
        listView: FrameLayout
    ): RecyclerView.ViewHolder(listView)
}
