package com.iwallic.app.states

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.PageDataPyModel
import com.iwallic.app.models.PageDataRes
import com.iwallic.app.models.TransactionRes
import com.iwallic.app.utils.HttpUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

object TransactionState {
    private var cached = PageDataPyModel<TransactionRes>()
    private var address: String = ""
    private var assetId: String = ""
    private val _list = PublishSubject.create<PageDataPyModel<TransactionRes>>()
    private val _error = PublishSubject.create<Int>()
    private val gson = Gson()
    var fetching: Boolean = false
    fun list(addr: String = "", asset: String? = null): Observable<PageDataPyModel<TransactionRes>> {
        if ((addr.isNotEmpty() && addr != address) || (asset != null && asset != assetId)) {
            fetch(addr, asset)
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
        resolveFetch(assetId, address, cached.page+1, cached.per_page, {
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
    fun fetch(addr: String = "", asset: String? = null, silent: Boolean = false) {
        if (fetching) {
            return
        }
        if (addr.isNotEmpty()) {
            Log.i("【TxState】", "set address")
            address = addr
        }
        if (asset != null) {
            Log.i("【TxState】", "set asset")
            assetId = asset
        }
        if (address.isEmpty()) {
            if (silent) {
                return
            }
            _error.onNext(99899)
        }
        fetching = true
        resolveFetch(assetId, address, 1, cached.per_page, {pageData ->
            if (silent) {
                val start = pageData.items.indexOfFirst {
                    it.txid == cached.items[0].txid
                }
                Log.i("【TxState】", "new tx from index【$start】")
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

    private fun resolveFetch(asset: String, addr: String, page: Int, size: Int, ok: (data: PageDataPyModel<TransactionRes>) -> Unit, no: (Int) -> Unit) {
        HttpUtils.getPy("/client/transaction/list?page=$page&wallet_address=$addr&assetId=$assetId&confirmed=true", {
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
//        HttpUtils.post(
//            if (assetId.isNotEmpty()) "getassettxes" else "getaccounttxes",
//            if (assetId.isNotEmpty()) listOf(page, size, addr, asset) else listOf(page, size, addr),
//            fun (res) {
//                if (res.isEmpty()) {
//                    ok(PageDataRes())
//                    return
//                }
//                val data = gson.fromJson<PageDataRes<TransactionRes>>(res, object: TypeToken<PageDataRes<TransactionRes>>() {}.type)
//                if (data == null) {
//                    no(99998)
//                } else {
//                    ok(data)
//                }
//            }, fun (err) {
//                no(err)
//            })
    }

    fun clear() {
        cached = PageDataPyModel()
        address = ""
        assetId = ""
        fetching = false
    }
}
