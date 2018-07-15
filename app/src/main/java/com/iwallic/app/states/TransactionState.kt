package com.iwallic.app.states

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.PageData
import com.iwallic.app.models.transactions
import com.iwallic.app.utils.HttpClient
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

object TransactionState {
    private var cached = PageData<transactions>()
    private var address: String = ""
    private var assetId: String = ""
    private val _list = PublishSubject.create<PageData<transactions>>()
    private val _error = PublishSubject.create<Int>()
    private val gson = Gson()
    var fetching: Boolean = false
    fun list(addr: String = "", asset: String? = null): Observable<PageData<transactions>> {
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
        if (cached.data.size >= cached.total || fetching || address.isEmpty()) {
            return
        }
        fetching = true
        resolveFetch(assetId, address, cached.page+1, cached.pageSize, {
            cached.page = it.page
            cached.total = it.total
            cached.pageSize = it.pageSize
            cached.data.addAll(it.data)
            Log.i("交易状态", "${cached.data.size} - ${it.data.size}")
            _list.onNext(cached)
            fetching = false
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
            Log.i("交易状态", "设置地址")
            address = addr
        }
        if (asset != null) {
            Log.i("交易状态", "设置资产为【$asset】")
            assetId = asset
        }
        if (address.isEmpty()) {
            if (silent) {
                return
            }
            _error.onNext(99899)
        }
        fetching = true
        resolveFetch(assetId, address, 1, cached.pageSize, {pageData ->
            if (silent) {
                val start = pageData.data.indexOfFirst {
                    it.txid == cached.data[0].txid
                }
                Log.i("交易状态", "从【$start】开始出现新交易")
                if (start > 0) {
                    pageData.data = ArrayList(pageData.data.subList(0, start))
                    pageData.data.addAll(cached.data)
                }
            }
            cached = pageData
            _list.onNext(cached)
            fetching = false
        }, {
            fetching = false
            if (silent) {
                return@resolveFetch
            }
            _error.onNext(it)
        })
    }

    private fun resolveFetch(asset: String, addr: String, page: Int, size: Int, ok: (data: PageData<transactions>) -> Unit, no: (Int) -> Unit) {
        HttpClient.post(
            if (assetId.isNotEmpty()) "getassettxes" else "getaccounttxes",
            if (assetId.isNotEmpty()) listOf(page, size, addr, asset) else listOf(page, size, addr),
            fun (res) {
                if (res.isEmpty()) {
                    ok(PageData())
                    return
                }
                val data = gson.fromJson<PageData<transactions>>(res, object: TypeToken<PageData<transactions>>() {}.type)
                if (data == null) {
                    no(99998)
                } else {
                    ok(data)
                }
            }, fun (err) {
                no(err)
            })
    }

    fun clear() {
        cached = PageData()
        address = ""
        assetId = ""
        fetching = false
    }
}
