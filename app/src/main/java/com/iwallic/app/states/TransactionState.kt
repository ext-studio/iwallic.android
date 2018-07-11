package com.iwallic.app.states

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.addrassets
import com.iwallic.app.models.pageData
import com.iwallic.app.models.transactions
import com.iwallic.app.utils.HttpClient
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlin.collections.ArrayList

object TransactionState {
    private var cached: pageData<transactions>? = null
    private var address: String = ""
    private val _list = PublishSubject.create<pageData<transactions>>()
    private val _error = PublishSubject.create<Int>()
    private val gson = Gson()
    private val size = 10
    private var fetching: Boolean = false
    fun list(addr: String = ""): Observable<pageData<transactions>> {
        if (addr.isNotEmpty() && addr != address) {
            fetch(addr)
        }
        if (cached != null) {
            Log.i("交易状态", "缓存获取")
            return _list.startWith(cached)
        }
        return _list
    }
    fun error(): Observable<Int> {
        return _error
    }
    fun fetch(addr: String = "", page: Int = 1, pageSize: Int = size,  silent: Boolean = false) {
        if (fetching) {
            return
        }
        if (addr.isNotEmpty()) {
            Log.i("交易状态", "设置地址")
            address = addr
        }
        if (address.isEmpty()) {
            if (silent) {
                return
            }
            _error.onNext(99899)
        }
        fetching = true
        HttpClient.post("getaccounttxes", listOf(page, pageSize, address), fun (res) {
            fetching = false
            if (res.isEmpty()) {
                cached = pageData(page, pageSize, 0)
                _list.onNext(cached!!)
                return
            }
            val data = gson.fromJson<pageData<transactions>>(res, object: TypeToken<pageData<transactions>>() {}.type)
            if (data == null) {
                if (silent) {
                    return
                }
                _error.onNext(99998)
            } else {
                cached = data
                _list.onNext(data)
            }
        }, fun (err) {
            fetching = false
            if (silent) {
                return
            }
            _error.onNext(err)
        })
    }
}
