package com.iwallic.app.states

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.PageDataPyModel
import com.iwallic.app.models.PageDataRes
import com.iwallic.app.models.TransactionRes
import com.iwallic.app.utils.ACache
import com.iwallic.app.utils.CommonUtils.pageCount
import com.iwallic.app.utils.HttpUtils
import com.iwallic.app.utils.SharedPrefUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

object TransactionState {
    private val gson = Gson()

    fun refresh(context: Context?, asset: String = "", force: Boolean = false): Observable<ArrayList<TransactionRes>> {
        return Observable.create { observer ->
            val aCache = ACache.get(context)
            val localStr = aCache.getAsString("tx_$asset")
            val localList = try {gson.fromJson<PageDataPyModel<TransactionRes>>(localStr, object: TypeToken<PageDataPyModel<TransactionRes>>() {}.type)} catch (_: Throwable) {null}
            if (localList != null && !force) {
                observer.onNext(localList.items)
                observer.onComplete()
                return@create
            }
            HttpUtils.getPy(context, "/client/transaction/list?page=1&page_size=$pageCount&wallet_address=${SharedPrefUtils.getAddress(context)}&asset_id=$asset&confirmed=true", {
                val data = try {gson.fromJson<PageDataPyModel<TransactionRes>>(it, object: TypeToken<PageDataPyModel<TransactionRes>>() {}.type)} catch (_: Throwable) {null}
                if (data == null) {
                    observer.onError(Throwable("99998"))
                } else {
                    aCache.put("tx_$asset", gson.toJson(data), ACache.TIME_DAY)
                    observer.onNext(data.items)
                    observer.onComplete()
                }
            }, {
                observer.onError(Throwable("$it"))
            })
        }
    }

    fun next(context: Context?, asset: String = ""): Observable<ArrayList<TransactionRes>> {
        return Observable.create { observer ->
            val aCache = ACache.get(context)
            val localStr = aCache.getAsString("tx_$asset")
            val localList = try {gson.fromJson<PageDataPyModel<TransactionRes>>(localStr, object: TypeToken<PageDataPyModel<TransactionRes>>() {}.type)} catch (_: Throwable) {null}
            if (localList == null) {
                observer.onNext(arrayListOf())
                observer.onComplete()
                return@create
            }
            HttpUtils.getPy(context, "/client/transaction/list?page=${localList.page+1}&page_size=$pageCount&wallet_address=${SharedPrefUtils.getAddress(context)}&asset_id=$asset&confirmed=true", {
                val data = try {gson.fromJson<PageDataPyModel<TransactionRes>>(it, object: TypeToken<PageDataPyModel<TransactionRes>>() {}.type)} catch (_: Throwable) {null}
                if (data == null) {
                    observer.onError(Throwable("99998"))
                } else {
                    observer.onNext(data.items)
                    data.items.addAll(0, localList.items)
                    aCache.put("tx_$asset", gson.toJson(data), ACache.TIME_DAY)
                    observer.onComplete()
                }
            }, {
                observer.onError(Throwable("$it"))
            })
        }
    }

//    fun list(context: Context?, addr: String = "", asset: String? = null): Observable<PageDataPyModel<TransactionRes>> {
//        if (cached == null || (addr.isNotEmpty() && addr != address) || (asset != null && asset != assetId)) {
//            fetch(context, addr, asset)
//            return _list
//        }
//        return _list.startWith(cached)
//    }
//    fun error(): Observable<Int> {
//        return _error
//    }
//    fun next(context: Context?) {
//        if (cached!!.page >= cached!!.pages || fetching || address.isEmpty()) {
//            return
//        }
//        fetching = true
//        resolveFetch(context, assetId, address, cached!!.page+1, cached!!.per_page, {
//            if (it.page > 1) {
//                cached!!.page = it.page
//                cached!!.total = it.total
//                cached!!.per_page = it.per_page
//                cached!!.items.addAll(it.items)
//            }
//            launch (UI) {
//                delay(500)
//                _list.onNext(it)
//                fetching = false
//            }
//        }, {
//            launch (UI) {
//                delay(500)
//                _error.onNext(it)
//                fetching = false
//            }
//        })
//    }
//    fun fetch(context: Context?, addr: String = "", asset: String? = null, silent: Boolean = false) {
//        if (fetching) {
//            return
//        }
//        if (addr.isNotEmpty()) {
//            Log.i("【TxState】", "set address")
//            address = addr
//        }
//        if (asset != null) {
//            Log.i("【TxState】", "set asset")
//            assetId = asset
//        }
//        if (address.isEmpty()) {
//            if (silent) {
//                return
//            }
//            _error.onNext(99899)
//        }
//        fetching = true
//        resolveFetch(context, assetId, address, 1, 15, {pageData ->
//            if (silent) {
//                val start = pageData.items.indexOfFirst {
//                    it.txid == cached!!.items[0].txid
//                }
//                Log.i("【TxState】", "new tx from index【$start】")
//                if (start > 0) {
//                    pageData.items = ArrayList(pageData.items.subList(0, start))
//                    pageData.items.addAll(cached!!.items)
//                }
//            }
//            cached = pageData
//            launch (UI) {
//                delay(500)
//                _list.onNext(cached!!)
//                fetching = false
//            }
//        }, {
//            launch (UI) {
//                delay(500)
//                if (!silent) {
//                    _error.onNext(it)
//                }
//                fetching = false
//            }
//        })
//    }
//
//    private fun resolveFetch(context: Context?, asset: String, addr: String, page: Int, size: Int, ok: (data: PageDataPyModel<TransactionRes>) -> Unit, no: (Int) -> Unit) {
//        HttpUtils.getPy(context, "/client/transaction/list?page=$page&page_size=$size&wallet_address=$addr&asset_id=$assetId&confirmed=true", {
//            if (it.isEmpty()) {
//                ok(PageDataPyModel())
//                return@getPy
//            }
//            val data = gson.fromJson<PageDataPyModel<TransactionRes>>(it, object: TypeToken<PageDataPyModel<TransactionRes>>() {}.type)
//            if (data == null) {
//                no(99998)
//            } else {
//                ok(data)
//            }
//        }, {
//            no(it)
//        })
//    }
//
//    fun clear() {
//        cached = null
//        address = ""
//        assetId = ""
//        fetching = false
//    }
}
