package com.iwallic.app.states

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.addrassets
import com.iwallic.app.utils.HttpClient
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlin.collections.ArrayList

object AssetState {
    private var cached: ArrayList<addrassets>? = null
    private var address: String = ""
    private val _list = PublishSubject.create<ArrayList<addrassets>>()
    private val _error = PublishSubject.create<Int>()
    private val gson = Gson()
    fun list(addr: String = ""): Observable<ArrayList<addrassets>> {
        if (addr.isNotEmpty() && addr != address) {
            fetch(addr)
        }
        if (cached != null) {
            return _list.startWith(cached)
        }
        return _list
    }
    fun error(): Observable<Int> {
        return _error
    }
    fun fetch(addr: String = "", silent: Boolean = false) {
        if (!addr.isEmpty()) {
            address = addr
        } else if (silent) {
            return
        }
        if (address.isEmpty()) {
            if (silent) {
                return
            }
            _error.onNext(99899)
        }
        HttpClient.post("getaddrassets", listOf(address, 1), fun (res) {
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
            if (silent) {
                return
            }
            _error.onNext(err)
        })
    }
}
