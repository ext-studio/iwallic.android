package com.iwallic.app.states

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.AssetRes
import com.iwallic.app.models.PageDataPyModel
import com.iwallic.app.utils.ACache
import com.iwallic.app.utils.CommonUtils
import com.iwallic.app.utils.HttpUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object AssetManageState {
    private const val take = 15
    private var currentPage = 1
    private val gson = Gson()
    /*
    try to get from local
    if nothing then request
     */
    fun init(context: Context?, ok: (ArrayList<AssetRes>) -> Unit, no: (Int) -> Unit) {
        currentPage = 1
        val aCache = ACache.get(context)
        val localData = local(aCache, currentPage)
        if (localData != null) {
            ok(localData)
        } else {
            refresh(context, ok, no)
        }
    }
    /*
    force request
     */
    fun refresh(context: Context?, ok: (ArrayList<AssetRes>) -> Unit, no: (Int) -> Unit) {
        currentPage = 1
        val aCache = ACache.get(context)
        fetch(context, currentPage, {
            set(aCache, it)
            ok(it.items)
        }, {
            no(it)
        })
    }
    /*
    try to get older
    if nothing then request
     */
    fun older(context: Context?, ok: (ArrayList<AssetRes>) -> Unit, no: (Int) -> Unit) {
        val next = currentPage + 1
        val aCache = ACache.get(context)
        val localData = local(aCache, next)
        if (localData != null && localData.isNotEmpty()) {
            currentPage = next
            ok(localData)
        } else {
            fetch(context, next, {
                if (it.items.size > 0) {
                    push(aCache, it)
                    currentPage = next
                }
                ok(it.items)
            }, {
                no(it)
            })
        }
    }

    fun addWatch(context: Context?, asset: AssetRes) {
        val aCache = ACache.get(context)
        var localData = try {gson.fromJson<ArrayList<AssetRes>>(aCache.getAsString("asset_manage_watch"), object: TypeToken<ArrayList<AssetRes>>() {}.type)} catch (_: Throwable) {null}
        if (localData == null) {
            localData = arrayListOf(asset)
        } else {
            localData.add(asset)
        }
        aCache.put("asset_manage_watch", gson.toJson(localData))
    }

    fun rmWatch(context: Context?, asset: AssetRes) {
        val aCache = ACache.get(context)
        val localData = try {gson.fromJson<ArrayList<AssetRes>>(aCache.getAsString("asset_manage_watch"), object: TypeToken<ArrayList<AssetRes>>() {}.type)} catch (_: Throwable) {null}
        localData?.removeAll { it.asset_id == asset.asset_id }
        aCache.put("asset_manage_watch", gson.toJson(localData))
    }

    fun watch(context: Context?): ArrayList<AssetRes> {
        val aCache = ACache.get(context)
        val localData = try {gson.fromJson<ArrayList<AssetRes>>(aCache.getAsString("asset_manage_watch"), object: TypeToken<ArrayList<AssetRes>>() {}.type)} catch (_: Throwable) {null}
        return localData ?: ArrayList()
    }

    private fun local(aCache: ACache, page: Int): ArrayList<AssetRes>? {
        val strRs = aCache.getAsString("asset_manage") ?: return null
        val rs = try {gson.fromJson<PageDataPyModel<AssetRes>>(strRs, object: TypeToken<PageDataPyModel<AssetRes>>() {}.type)} catch (_: Throwable) {null}
        return if (rs == null) {
            null
        } else {
            CommonUtils.safeSlice(rs.items, (page-1)*take, take)
        }
    }

    private fun push(aCache: ACache, data: PageDataPyModel<AssetRes>) {
        val strRs = aCache.getAsString("asset_manage") ?: return
        val rs = try {gson.fromJson<PageDataPyModel<AssetRes>>(strRs, object: TypeToken<PageDataPyModel<AssetRes>>() {}.type)} catch (_: Throwable) {null}
        if (rs == null) {
            aCache.put("asset_manage", gson.toJson(data))
        } else {
            rs.pages = data.pages
            rs.total = data.total
            rs.page = data.page
            rs.items.addAll(data.items)
            aCache.put("asset_manage", gson.toJson(rs))
        }
    }

    private fun set(aCache: ACache, data: PageDataPyModel<AssetRes>) {
        aCache.put("asset_manage", gson.toJson(data))
    }

    private fun fetch(context: Context?, page: Int, ok: (PageDataPyModel<AssetRes>) -> Unit, no: (Int) -> Unit) {
        HttpUtils.getPy(context, "/client/assets/list?page=$page&page_size=$take", {
            val rs = try {gson.fromJson<PageDataPyModel<AssetRes>>(it, object: TypeToken<PageDataPyModel<AssetRes>>() {}.type)} catch (_: Throwable) {null}
            ok(rs ?: PageDataPyModel())
        }, {
            no(it)
        })
    }
}