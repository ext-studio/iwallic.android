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
            "81c089ab996fc89c468a26c0a88d23ae2f34b5c0",
            "7f86d61ff377f1b12e589a5907152b57e2ad9a7a",
            "0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b",
            "0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7"
        ).contains(data.items[position].assetId)) {
            toggleSC.visibility = View.GONE
        } else {
            toggleSC.isChecked = display.indexOfFirst {
                it.assetId == data.items[position].assetId
            } >= 0
            toggleSC.setOnClickListener {
                if (toggleSC.isChecked) {
                    SharedPrefUtils.addAsset(holder.itemView.context, data.items[position])
                    Log.i("【AssetManage】", "switch【${data.items[position].symbol}】to【on】")
                } else {
                    SharedPrefUtils.rmAsset(holder.itemView.context, data.items[position].assetId)
                    Log.i("【AssetManage】", "switch【${data.items[position].symbol}】to【off】")
                }
            }
        }
    }

    // 07-23 03:43:58.050 6504-6504/com.iwallic.app I/【request】: complete【get】【/client/assets/list?page=1&wallet_address=AaLWvC9jgPG9MkTZGaXbWrTxgE6zXLhKUV】【{"bool_status": true, "data": {"items": [{"assetId": "81c089ab996fc89c468a26c0a88d23ae2f34b5c0", "create_at": null, "display": false, "id": 3, "ip": null, "logo_path": "/upload_logo/0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7caa.jpg", "name": "EXT", "pinned": true, "sort_number": 9, "symbol": "EXT", "update_at": "2018-07-11 18:28:50", "visiable": true}, {"assetId": "7f86d61ff377f1b12e589a5907152b57e2ad9a7a", "create_at": null, "display": false, "id": 4, "ip": null, "logo_path": "/upload_logo/7f86d61ff377f1b12e589a5907152b57e2ad9a7a.jpg", "name": "EDS", "pinned": true, "sort_number": 2, "symbol": "EDS", "update_at": "2018-07-18 15:39:31", "visiable": true}, {"assetId": "0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "create_at": null, "display": false, "id": 1, "ip": null, "logo_path": "/upload_logo/0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b.jpg", "name": "NEO", "pinned": true, "sort_number": 1, "symbol": "NEO", "update_at": "2018-07-18 15:01:32", "visiable": true}, {"assetId": "0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7", "create_at": null, "display": false, "id": 2, "ip": null, "logo_path": "/upload_logo/0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b.jpg", "name": "GAS", "pinned": false, "sort_number": 0, "symbol": "GAS", "update_at": "2018-07-11 18:27:55", "visiable": true}, {"assetId": "a0777c3ce2b169d4a23bcba4565e3225a0122d95", "create_at": null, "display": false, "id": 5, "ip": null, "logo_path": "/upload_logo/a0777c3ce2b169d4a23bcba4565e3225a0122d95.jpg", "name": null, "pinned": false, "sort_number": 0, "symbol": "ACAT", "update_at": "2018-07-18 15:43:01", "visiable": true}, {"assetId": "0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "create_at": null, "display": false, "id": 6, "ip": null, "logo_path": "/upload_logo/0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b.jpg", "name": "NEO", "pinned": false, "sort_number": 0, "symbol": "APT", "update_at": "2018-07-18 15:49:02", "visiable": true}, {"assetId": "0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "create_at": null, "display": false, "id": 7, "ip": null, "logo_path": null, "name": "NEO", "pinned": false, "sort_number": 0, "symbol": "NEO", "update_at": null, "visiable": true}, {"assetId": "0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "create_at": null, "display": false, "id": 8, "ip": null, "logo_path": null, "name": "NEO", "pinned": false, "sort_number": 0, "symbol": "NEO", "update_at": null, "visiable": true}, {"assetId": "0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7", "create_at": null, "display": false, "id": 9, "ip": null, "logo_path": null, "name": "GAS", "pinned": false, "sort_number": 0, "symbol": "GAS", "update_at": null, "visiable": true}, {"assetId": "e8f98440ad0d7a6e76d84fb1c3d3f8a16e162e97", "create_at": null, "display": false, "id": 10, "ip": null, "logo_path": null, "name": "Experience Token", "pinned": false, "sort_number": 0, "symbol": "EXT", "update_at": null, "visiable": true}], "page": 1, "pages": 8, "per_page": 10, "total": 71}, "msg": "ok"}】


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