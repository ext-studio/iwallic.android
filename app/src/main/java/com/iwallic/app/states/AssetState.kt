package com.iwallic.app.states

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.addrassets
import com.iwallic.app.utils.ConfigUtils
import com.iwallic.app.utils.HttpClient
import com.iwallic.app.utils.SharedPrefUtils
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlin.collections.ArrayList

object AssetState {
    var cached: ArrayList<addrassets>? = null
    private var address: String = ""
    private val _list = PublishSubject.create<ArrayList<addrassets>>()
    private val _error = PublishSubject.create<Int>()
    var fetching: Boolean = false
    private val gson = Gson()
    fun list(addr: String = ""): Observable<ArrayList<addrassets>> {
        if (addr.isNotEmpty() && addr != address) {
            fetch(addr)
            return _list
        }
        if (cached != null) {
            Log.i("资产状态", "缓存获取")
            return _list.startWith(cached)
        }
        return _list
    }
    fun get(id: String): addrassets? = cached?.find {
        it.assetId == id
    }
    fun error(): Observable<Int> {
        return _error
    }
    fun fetch(addr: String = "", silent: Boolean = false) {
        if (fetching) {
            return
        }
        if (addr.isNotEmpty()) {
            Log.i("资产状态", "设置地址")
            address = addr
        }
        if (address.isEmpty()) {
            if (silent) {
                return
            }
            _error.onNext(99899)
        }
        fetching = true
        HttpClient.post("getaddrassets", listOf(address, 1), fun (res) {
            fetching = false
            val data = gson.fromJson<ArrayList<addrassets>>(res, object: TypeToken<ArrayList<addrassets>>() {}.type)
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
    fun clear() {
        cached = null
        address = ""
        fetching = false
    }
}
