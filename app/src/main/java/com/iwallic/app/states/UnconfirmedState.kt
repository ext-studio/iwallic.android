package com.iwallic.app.states

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.PageDataPyModel
import com.iwallic.app.models.TransactionRes
import com.iwallic.app.utils.HttpUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

object UnconfirmedState {
    private var cached = PageDataPyModel<TransactionRes>()
    private var address: String = ""
    private val _list = PublishSubject.create<PageDataPyModel<TransactionRes>>()
    private val _error = PublishSubject.create<Int>()
    private val gson = Gson()
    var fetching: Boolean = false
    fun has(): Boolean {
        return cached.items.size > 0
    }
    fun list(addr: String = ""): Observable<PageDataPyModel<TransactionRes>> {
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
            Log.i("【UnconfirmedState】", "set address")
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
            if (silent) {
                val start = pageData.items.indexOfFirst {
                    it.txid == cached.items[0].txid
                }
                Log.i("【UnconfirmedState】", "new tx from index【$start】")
                if (start > 0) {
                    pageData.items = ArrayList(pageData.items.subList(0, start))
                    pageData.items.addAll(cached.items)
                }
            }
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

    private fun resolveFetch(addr: String, page: Int, size: Int, ok: (data: PageDataPyModel<TransactionRes>) -> Unit, no: (Int) -> Unit) {
        HttpUtils.getPy("/client/transaction/list?page=$page&per_page=$size&wallet_address=$addr&confirmed=false", {
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
        cached = PageDataPyModel()
        address = ""
        fetching = false
    }
}
