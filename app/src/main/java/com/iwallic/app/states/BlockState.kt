package com.iwallic.app.states

import android.util.Log
import com.google.gson.Gson
import com.iwallic.app.models.blocktime
import com.iwallic.app.utils.HttpClient
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

object BlockState {
    private var height: Long = 0
    private val _data = PublishSubject.create<blocktime>()
    private val _error = PublishSubject.create<Int>()
    private var loading: Boolean = false
    private val gson = Gson()
    fun data(): Observable<blocktime> {
        Log.i("区块状态", "订阅区块")
        return _data
    }
    fun fetch() {
        if (loading) {
            return
        }
        loading = true
        HttpClient.post("getblocktime", emptyList(), fun (res) {
            loading = false
            val blockData = gson.fromJson(res, blocktime::class.java)
            if (blockData == null) {
                _error.onNext(99998)
            } else {
                if (blockData.lastBlockIndex > height) {
                    height = blockData.lastBlockIndex
                    _data.onNext(blockData)
                } else {
                    _error.onNext(99799)
                }
            }
        }, fun (err) {
            loading = false
            _error.onNext(err)
        })
    }
}
