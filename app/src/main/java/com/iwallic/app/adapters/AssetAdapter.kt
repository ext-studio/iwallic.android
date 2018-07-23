package com.iwallic.app.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.models.AssetRes
import com.iwallic.app.utils.ImageUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class AssetAdapter(list: ArrayList<AssetRes>): RecyclerView.Adapter<AssetAdapter.ViewHolder>() {
    private var data = list
    private val _onClick = PublishSubject.create<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_asset_list, parent, false) as FrameLayout

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // ImageUtils.setUrl(holder.itemView.findViewById<ImageView>(R.id.asset_list_logo), data[position].logo)
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

    fun set(newData: ArrayList<AssetRes>) {
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
