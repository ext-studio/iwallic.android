package com.iwallic.app.states

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.AssetRes
import com.iwallic.app.utils.HttpUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlin.collections.ArrayList

object AssetState {
    var cached: ArrayList<AssetRes>? = null
    private var address: String = ""
    private val _list = PublishSubject.create<ArrayList<AssetRes>>()
    private val _error = PublishSubject.create<Int>()
    var fetching: Boolean = false
    private val gson = Gson()
    fun list(addr: String = ""): Observable<ArrayList<AssetRes>> {
        if (addr.isNotEmpty() && addr != address) {
            fetch(addr)
            return _list
        }
        if (cached != null) {
            Log.i("【AssetState】", "from cache")
            return _list.startWith(cached)
        } else {
            fetch()
        }
        return _list
    }
    fun get(id: String): AssetRes? = cached?.find {
        it.asset_id == id
    }
    fun error(): Observable<Int> {
        return _error
    }
    fun fetch(addr: String = "", silent: Boolean = false) {
        if (fetching) {
            return
        }
        if (addr.isNotEmpty()) {
            Log.i("【AssetState】", "set address")
            address = addr
        }
        if (address.isEmpty()) {
            if (silent) {
                return
            }
            _error.onNext(99899)
        }
        fetching = true
        HttpUtils.getPy("/client/index/assets/display?wallet_address=$address", {
            fetching = false
            val data = gson.fromJson<ArrayList<AssetRes>>(it, object: TypeToken<ArrayList<AssetRes>>() {}.type)
            if (data == null) {
                if (silent) {
                    return@getPy
                }
                _error.onNext(99998)
            } else {
                cached = data
                _list.onNext(data)
            }
        }, {
            fetching = false
            if (silent) {
                return@getPy
            }
            _error.onNext(it)
        })
    }
    fun clear() {
        cached = null
        address = ""
        fetching = false
    }
}
