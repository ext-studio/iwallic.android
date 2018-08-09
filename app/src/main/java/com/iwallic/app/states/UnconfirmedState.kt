package com.iwallic.app.states

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.PageDataPyModel
import com.iwallic.app.models.TransactionRes
import com.iwallic.app.utils.HttpUtils
import com.iwallic.app.utils.SharedPrefUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

object UnconfirmedState {
    private var cached: PageDataPyModel<TransactionRes>? = null
    private var address: String = ""
    private val _list = PublishSubject.create<PageDataPyModel<TransactionRes>>()
    private val _error = PublishSubject.create<Int>()
    private val gson = Gson()
    var fetching: Boolean = false
    fun has(): Boolean {
        return cached?.items!!.size > 0
    }
    fun list(addr: String = ""): Observable<PageDataPyModel<TransactionRes>> {
        if (cached == null || (addr.isNotEmpty() && addr != address)) {
            fetch(addr)
            return _list
        }
        return _list.startWith(cached)
    }
    fun error(): Observable<Int> {
        return _error
    }
    fun next() {
        if (cached!!.page >= cached!!.pages || fetching || address.isEmpty()) {
            return
        }
        fetching = true
        resolveFetch(address, cached!!.page+1, cached!!.per_page, {
            if (it.page > 1) {
                cached!!.page = it.page
                cached!!.total = it.total
                cached!!.per_page = it.per_page
                cached!!.items.addAll(it.items)
            }
            launch (UI) {
                delay(500)
                _list.onNext(it)
                fetching = false
            }
        }, {
            launch (UI) {
                delay(500)
                _error.onNext(it)
                fetching = false
            }
        })
    }
    fun fetch(addr: String = "", context: Context? = null) {
        if (fetching) {
            return
        }
        if (addr.isNotEmpty()) {
            Log.i("【UnconfirmedState】", "set address")
            address = addr
        }
        if (address.isEmpty()) {
            _error.onNext(99899)
        }
        fetching = true
        resolveFetch(address, 1, 15, {pageData ->
            cached = pageData
            if (context != null) {
                resolveClaim(context)
            }
            launch (UI) {
                delay(500)
                _list.onNext(cached!!)
                fetching = false
            }
        }, {
            launch (UI) {
                delay(500)
                _error.onNext(it)
                fetching = false
            }
        })
    }

    private fun resolveClaim(context: Context) {
        val claim = SharedPrefUtils.getClaim(context)
        val collect = SharedPrefUtils.getCollect(context)
        if (claim.isNotEmpty()) {
            if (cached!!.items.indexOfFirst {
                it.txid == claim
            } < 0) {
                Log.i("【Unconfirmed】", "claim complete【$claim】")
                SharedPrefUtils.setClaim(context, "")
            } else {
                Log.i("【Unconfirmed】", "claim incomplete【$claim】")
            }
        }
        if (collect.isNotEmpty()) {
            if (cached!!.items.indexOfFirst {
                it.txid == collect
            } < 0) {
                Log.i("【Unconfirmed】", "collect complete【$collect】")
                SharedPrefUtils.setCollect(context, "")
            } else {
                Log.i("【Unconfirmed】", "collect incomplete【$collect】")
            }
        }
    }

    private fun resolveFetch(addr: String, page: Int, size: Int, ok: (data: PageDataPyModel<TransactionRes>) -> Unit, no: (Int) -> Unit) {
        HttpUtils.getPy("/client/transaction/list?page=$page&page_size=$size&wallet_address=$addr&confirmed=false", {
            if (it.isEmpty()) {
                ok(PageDataPyModel())
                return@getPy
            }
            val data = gson.fromJson<PageDataPyModel<TransactionRes>>(it, object: TypeToken<PageDataPyModel<TransactionRes>>() {}.type)
            if (data == null) {
                no(99998)
            } else {
                ok(data)
            }
        }, {
            no(it)
        })
    }

    fun clear() {
        cached = null
        address = ""
        fetching = false
    }
}
