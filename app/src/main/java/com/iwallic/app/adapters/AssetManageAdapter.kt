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
import com.iwallic.app.utils.SharedPrefUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class AssetManageAdapter(_data: PageDataPyModel<AssetRes>, _display: ArrayList<AssetRes>): RecyclerView.Adapter<AssetManageAdapter.ViewHolder>() {
    private var data = _data
    private var display = _display
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
            pagerTV.setText(if (data.items.size < data.total) R.string.list_loadmore else R.string.list_nomore)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == data.items.size) {
            return
        }
        holder.itemView.findViewById<TextView>(R.id.asset_manage_name).text = data.items[position].symbol
        val toggleSC = holder.itemView.findViewById<SwitchCompat>(R.id.asset_manage_toggle)
        if (listOf(
            "e8f98440ad0d7a6e76d84fb1c3d3f8a16e162e97",
            "81c089ab996fc89c468a26c0a88d23ae2f34b5c0",
            "0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b",
            "0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7"
        ).contains(data.items[position].asset_id)) {
            Log.i("【】", data.items[position].asset_id)
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

    override fun getItemCount() = data.items.size + 1

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == data.items.size) VIEW_TYPE_FOOTER else VIEW_TYPE_CELL
    }

    fun push(newData: PageDataPyModel<AssetRes>) {
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
        return position == data.items.size && data.items.size < data.total
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

    fun getItem(position: Int): AssetRes {
        return data.items[position]
    }

    class ViewHolder(
        listView: FrameLayout
    ): RecyclerView.ViewHolder(listView)
}