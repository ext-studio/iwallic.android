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
    private val gson = Gson()

    fun refresh(context: Context?, force: Boolean = false): Observable<ArrayList<TransactionRes>> {
        return Observable.create { observer ->
            val aCache = ACache.get(context)
            val localStr = aCache.getAsString("tx_unconfirmed")
            val localList = try {gson.fromJson<PageDataPyModel<TransactionRes>>(localStr, object: TypeToken<PageDataPyModel<TransactionRes>>() {}.type)} catch (_: Throwable) {null}
            if (localList != null && !force) {
                observer.onNext(localList.items)
                observer.onComplete()
                return@create
            }
            HttpUtils.getPy(context, "/client/transaction/list?page=1&page_size=${CommonUtils.pageCount}&wallet_address=${SharedPrefUtils.getAddress(context)}&confirmed=false", {
                val data = try {gson.fromJson<PageDataPyModel<TransactionRes>>(it, object: TypeToken<PageDataPyModel<TransactionRes>>() {}.type)} catch (_: Throwable) {null}
                if (data == null) {
                    observer.onError(Throwable("99998"))
                } else {
                    aCache.put("tx_unconfirmed", gson.toJson(data), ACache.TIME_DAY)
                    observer.onNext(data.items)
                    observer.onComplete()
                }
            }, {
                observer.onError(Throwable("$it"))
            })
        }
    }

    fun next(context: Context?): Observable<ArrayList<TransactionRes>> {
        return Observable.create { observer ->
            val aCache = ACache.get(context)
            val localStr = aCache.getAsString("tx_unconfirmed")
            val localList = try {gson.fromJson<PageDataPyModel<TransactionRes>>(localStr, object: TypeToken<PageDataPyModel<TransactionRes>>() {}.type)} catch (_: Throwable) {null}
            if (localList == null) {
                observer.onNext(arrayListOf())
                observer.onComplete()
                return@create
            }
            HttpUtils.getPy(context, "/client/transaction/list?page=${localList.page+1}&page_size=${CommonUtils.pageCount}&wallet_address=${SharedPrefUtils.getAddress(context)}&confirmed=false", {
                val data = try {gson.fromJson<PageDataPyModel<TransactionRes>>(it, object: TypeToken<PageDataPyModel<TransactionRes>>() {}.type)} catch (_: Throwable) {null}
                if (data == null) {
                    observer.onError(Throwable("99998"))
                } else {
                    observer.onNext(data.items)
                    data.items.addAll(0, localList.items)
                    aCache.put("tx_unconfirmed", gson.toJson(data), ACache.TIME_DAY)
                    observer.onComplete()
                }
            }, {
                observer.onError(Throwable("$it"))
            })
        }
    }
}
