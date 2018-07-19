package com.iwallic.app.adapters

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.iwallic.app.R
import com.iwallic.app.models.AssetManageRes
import com.iwallic.app.models.PageDataPyModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class AssetManageAdapter(_data: PageDataPyModel<AssetManageRes>): RecyclerView.Adapter<AssetManageAdapter.ViewHolder>() {
    private var data = _data
    private val _onSwitch = PublishSubject.create<Int>()
    private val VIEW_TYPE_CELL = 1
    private val VIEW_TYPE_FOOTER = 0
    private lateinit var pagerTV: TextView
    private var paging: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: FrameLayout
        if (viewType == VIEW_TYPE_CELL) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_asset_manage, parent, false) as FrameLayout
        } else {
            view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_pager, parent, false) as FrameLayout
            pagerTV = view.findViewById(R.id.adapter_pager)
            pagerTV.setText(if (data.items.size != data.total) R.string.list_loadmore else R.string.list_nomore)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == data.items.size) {
            return
        }
        holder.itemView.findViewById<TextView>(R.id.asset_manage_name).text = data.items[position].symbol
        val toggleSC = holder.itemView.findViewById<SwitchCompat>(R.id.asset_manage_toggle)
        toggleSC.isChecked = data.items[position].display
        toggleSC.setOnClickListener {
            Log.i("【AssetManage】", "switch【${data.items[position].symbol}】")
            _onSwitch.onNext(position)
        }
    }

    override fun getItemCount() = data.items.size + 1

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == data.items.size) VIEW_TYPE_FOOTER else VIEW_TYPE_CELL
    }

    fun onSwitch(): Observable<Int> {
        return _onSwitch
    }

    fun push(newData: PageDataPyModel<AssetManageRes>) {
        if (newData.page == 1) {
            data = newData
            notifyDataSetChanged()
        } else {
            val p = data.items.size
            data.page = newData.page
            data.pages = newData.pages
            data.total = newData.total
            data.per_page = newData.per_page
            data.items.addAll(newData.items)
            notifyItemRangeInserted(p + 1, newData.items.size)
        }
        pagerTV.setText(if (data.items.size < data.total) R.string.list_loadmore else R.string.list_nomore)
        paging = false
    }

    fun checkNext(position: Int): Boolean {
        if (paging) {
            return false
        }
        return position == data.items.size && data.items.size != data.total
    }

    fun getPage(): Int {
        return data.page
    }

    fun setPaging() {
        if (paging) {
            return
        }
        paging = true
        pagerTV.setText(R.string.list_loading)
    }

    fun getItem(position: Int): AssetManageRes {
        return data.items[position]
    }

    fun setChecked(position: Int, checked: Boolean) {
        data.items[position].display = checked
        notifyItemChanged(position)
    }

    class ViewHolder(
        listView: FrameLayout
    ): RecyclerView.ViewHolder(listView)
}