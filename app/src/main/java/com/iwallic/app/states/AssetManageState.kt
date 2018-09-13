package com.iwallic.app.states

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.AssetRes
import com.iwallic.app.models.PageDataPyModel
import com.iwallic.app.utils.HttpUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

object AssetManageState {
    private var cached: PageDataPyModel<AssetRes>? = null
    private var address: String = ""
    private val _list = PublishSubject.create<PageDataPyModel<AssetRes>>()
    private val _error = PublishSubject.create<Int>()
    private val gson = Gson()
    var fetching: Boolean = false
    fun list(context: Context?, addr: String = ""): Observable<PageDataPyModel<AssetRes>> {
        if (cached == null || (addr.isNotEmpty() && addr != address)) {
            fetch(context, addr)
            return _list
        }
        return _list.startWith(cached!!)
    }
    fun error(): Observable<Int> {
        return _error
    }
    fun next(context: Context?) {
        if (cached!!.page >= cached!!.pages) {
            return
        }
        fetching = true
        resolveFetch(context, address, cached!!.page+1, cached!!.per_page, {
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
    fun fetch(context: Context?, addr: String = "", silent: Boolean = false) {
        if (fetching) {
            return
        }
        if (addr.isNotEmpty()) {
            Log.i("【TxState】", "set address")
            address = addr
        }
        if (address.isEmpty()) {
            if (silent) {
                return
            }
            _error.onNext(99899)
        }
        fetching = true
        resolveFetch(context, address, 1, 15, {pageData ->
            cached = pageData
            launch (UI) {
                delay(500)
                _list.onNext(cached!!)
                fetching = false
            }
        }, {
            launch (UI) {
                delay(500)
                if (!silent) {
                    _error.onNext(it)
                }
                fetching = false
            }
        })
    }

    private fun resolveFetch(context: Context?, addr: String, page: Int, size: Int, ok: (data: PageDataPyModel<AssetRes>) -> Unit, no: (Int) -> Unit) {
        HttpUtils.getPy(context, "/client/assets/list?page=$page&page_size=$size&wallet_address=$addr", {
            if (it.isEmpty()) {
                ok(PageDataPyModel())
                return@getPy
            }
            val data = gson.fromJson<PageDataPyModel<AssetRes>>(it, object: TypeToken<PageDataPyModel<AssetRes>>() {}.type)
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