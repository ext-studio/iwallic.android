package com.iwallic.app.states

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.AssetRes
import com.iwallic.app.models.PageDataPyModel
import com.iwallic.app.utils.HttpUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

object AssetManageState {
    private var cached = PageDataPyModel<AssetRes>()
    private var address: String = ""
    private val _list = PublishSubject.create<PageDataPyModel<AssetRes>>()
    private val _error = PublishSubject.create<Int>()
    private val gson = Gson()
    var fetching: Boolean = false
    fun list(addr: String = ""): Observable<PageDataPyModel<AssetRes>> {
        if ((addr.isNotEmpty() && addr != address)) {
            fetch(addr)
            return _list
        }
        return _list.startWith(cached)
    }
    fun error(): Observable<Int> {
        return _error
    }
    fun next() {
        if (cached.items.size >= cached.total || fetching || address.isEmpty()) {
            return
        }
        fetching = true
        resolveFetch(address, cached.page+1, cached.per_page, {
            if (it.page > 1) {
                cached.page = it.page
                cached.total = it.total
                cached.per_page = it.per_page
                cached.items.addAll(it.items)
                _list.onNext(it)
            }
            launch {
                delay(1000)
                fetching = false
            }
        }, {
            _error.onNext(it)
            launch {
                delay(1000)
                fetching = false
            }
        })
    }
    fun fetch(addr: String = "", silent: Boolean = false) {
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
        resolveFetch(address, 1, cached.per_page, {pageData ->
            cached = pageData
            _list.onNext(cached)
            launch {
                delay(1000)
                fetching = false
            }
        }, {
            launch {
                delay(1000)
                fetching = false
            }
            if (silent) {
                return@resolveFetch
            }
            _error.onNext(it)
        })
    }

    private fun resolveFetch(addr: String, page: Int, size: Int, ok: (data: PageDataPyModel<AssetRes>) -> Unit, no: (Int) -> Unit) {
        HttpUtils.getPy("/client/assets/list?page=$page&page_size=$size&wallet_address=$addr", {
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
        cached = PageDataPyModel()
        address = ""
        fetching = false
    }
}