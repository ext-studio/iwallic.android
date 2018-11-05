package com.iwallic.app.states

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.PageDataPyModel
import com.iwallic.app.models.PageDataRes
import com.iwallic.app.models.TransactionRes
import com.iwallic.app.utils.ACache
import com.iwallic.app.utils.CommonUtils
import com.iwallic.app.utils.CommonUtils.pageCount
import com.iwallic.app.utils.HttpUtils
import com.iwallic.app.utils.SharedPrefUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object TransactionState {
    private const val take = 15
    private var currentPage = 1
    private val gson = Gson()

    fun init(context: Context?, asset: String = "", ok: (ArrayList<TransactionRes>) -> Unit, no: (Int) -> Unit) {
        currentPage = 1
        val aCache = ACache.get(context)
        val localData = local(aCache, asset, currentPage)
        if (localData != null) {
            ok(localData)
        } else {
            refresh(context, asset, ok, no)
        }
    }

    fun refresh(context: Context?, asset: String = "", ok: (ArrayList<TransactionRes>) -> Unit, no: (Int) -> Unit) {
        currentPage = 1
        val aCache = ACache.get(context)
        fetch(context, asset, currentPage, {
            set(aCache, asset, it)
            ok(it.items)
        }, {
            no(it)
        })
    }

    fun older(context: Context?, asset: String = "", ok: (ArrayList<TransactionRes>) -> Unit, no: (Int) -> Unit) {
        val next = currentPage + 1
        val aCache = ACache.get(context)
        val localData = local(aCache, asset, next)
        if (localData != null && localData.isNotEmpty()) {
            currentPage = next
            ok(localData)
        } else {
            fetch(context, asset, next, {
                if (it.items.size > 0) {
                    push(aCache, asset, it)
                    currentPage = next
                }
                ok(it.items)
            }, {
                no(it)
            })
        }
    }

    private fun local(aCache: ACache, asset: String, page: Int): ArrayList<TransactionRes>? {
        val strRs = aCache.getAsString("tx_$asset") ?: return null
        val rs = try {gson.fromJson<PageDataPyModel<TransactionRes>>(strRs, object: TypeToken<PageDataPyModel<TransactionRes>>() {}.type)} catch (_: Throwable) {null}
        return if (rs == null) {
            null
        } else {
            CommonUtils.safeSlice(rs.items, (page-1)*take, take)
        }
    }

    private fun push(aCache: ACache, asset: String, data: PageDataPyModel<TransactionRes>) {
        val strRs = aCache.getAsString("tx_$asset") ?: return
        val rs = try {gson.fromJson<PageDataPyModel<TransactionRes>>(strRs, object: TypeToken<PageDataPyModel<TransactionRes>>() {}.type)} catch (_: Throwable) {null}
        if (rs == null) {
            aCache.put("tx_$asset", gson.toJson(data))
        } else {
            rs.pages = data.pages
            rs.total = data.total
            rs.page = data.page
            rs.items.addAll(data.items)
            aCache.put("tx_$asset", gson.toJson(rs))
        }
    }

    private fun set(aCache: ACache, asset: String, data: PageDataPyModel<TransactionRes>) {
        aCache.put("tx_$asset", gson.toJson(data))
    }

    private fun fetch(context: Context?, asset: String, page: Int, ok: (PageDataPyModel<TransactionRes>) -> Unit, no: (Int) -> Unit) {
        HttpUtils.getPy(context, "/client/transaction/list?page=$page&page_size=$take&wallet_address=${SharedPrefUtils.getAddress(context)}&asset_id=$asset&confirmed=true", {
            val rs = try {gson.fromJson<PageDataPyModel<TransactionRes>>(it, object: TypeToken<PageDataPyModel<TransactionRes>>() {}.type)} catch (_: Throwable) {null}
            ok(rs ?: PageDataPyModel())
        }, {
            no(it)
        })
    }
}
