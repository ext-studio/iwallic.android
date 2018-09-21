package com.iwallic.app.states

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.PageDataPyModel
import com.iwallic.app.models.TransactionRes
import com.iwallic.app.utils.ACache
import com.iwallic.app.utils.CommonUtils
import com.iwallic.app.utils.HttpUtils
import com.iwallic.app.utils.SharedPrefUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

object UnconfirmedState {
    private const val take = 15
    private var currentPage = 1
    private val gson = Gson()

    fun init(context: Context?, ok: (ArrayList<TransactionRes>) -> Unit, no: (Int) -> Unit) {
        currentPage = 1
        val aCache = ACache.get(context)
        val localData = local(aCache, currentPage)
        if (localData != null) {
            ok(localData)
        } else {
            refresh(context, ok, no)
        }
    }

    fun refresh(context: Context?, ok: (ArrayList<TransactionRes>) -> Unit, no: (Int) -> Unit) {
        currentPage = 1
        val aCache = ACache.get(context)
        fetch(context, currentPage, {
            set(aCache, it)
            ok(it.items)
        }, {
            no(it)
        })
    }

    fun older(context: Context?, ok: (ArrayList<TransactionRes>) -> Unit, no: (Int) -> Unit) {
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

    fun clear(context: Context?) {
        ACache.get(context).remove("tx_unconfirmed")
    }

    private fun local(aCache: ACache, page: Int): ArrayList<TransactionRes>? {
        val strRs = aCache.getAsString("tx_unconfirmed") ?: return null
        val rs = try {gson.fromJson<PageDataPyModel<TransactionRes>>(strRs, object: TypeToken<PageDataPyModel<TransactionRes>>() {}.type)} catch (_: Throwable) {null}
        return if (rs == null) {
            null
        } else {
            CommonUtils.safeSlice(rs.items, (page-1)*take, take)
        }
    }

    private fun push(aCache: ACache, data: PageDataPyModel<TransactionRes>) {
        val strRs = aCache.getAsString("tx_unconfirmed") ?: return
        val rs = try {gson.fromJson<PageDataPyModel<TransactionRes>>(strRs, object: TypeToken<PageDataPyModel<TransactionRes>>() {}.type)} catch (_: Throwable) {null}
        if (rs == null) {
            aCache.put("tx_unconfirmed", gson.toJson(data))
        } else {
            rs.pages = data.pages
            rs.total = data.total
            rs.page = data.page
            rs.items.addAll(data.items)
            aCache.put("tx_unconfirmed", gson.toJson(rs))
        }
    }

    private fun set(aCache: ACache, data: PageDataPyModel<TransactionRes>) {
        aCache.put("tx_unconfirmed", gson.toJson(data))
    }

    private fun fetch(context: Context?, page: Int, ok: (PageDataPyModel<TransactionRes>) -> Unit, no: (Int) -> Unit) {
        HttpUtils.getPy(context, "/client/transaction/list?page=$page&page_size=$take&wallet_address=${SharedPrefUtils.getAddress(context)}&confirmed=false", {
            val rs = try {gson.fromJson<PageDataPyModel<TransactionRes>>(it, object: TypeToken<PageDataPyModel<TransactionRes>>() {}.type)} catch (_: Throwable) {null}
            ok(rs ?: PageDataPyModel())
        }, {
            no(it)
        })
    }
}
