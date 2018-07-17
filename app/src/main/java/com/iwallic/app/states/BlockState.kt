package com.iwallic.app.states

import com.google.gson.Gson
import com.iwallic.app.models.BlockTimeRes
import com.iwallic.app.utils.HttpClient
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object BlockState {
    private var height: Long = 0
    private val _data = PublishSubject.create<BlockTimeRes>()
    private val _error = PublishSubject.create<Int>()
    private var loading: Boolean = false
    private val gson = Gson()
    fun data(): Observable<BlockTimeRes> {
        return _data
    }
    fun error(): Observable<Int> {
        return _error
    }
    fun fetch() {
        if (loading) {
            return
        }
        loading = true
        HttpClient.post("getblocktime", emptyList(), fun (res) {
            loading = false
            val blockData = gson.fromJson(res, BlockTimeRes::class.java)
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
    fun clear() {
        height = 0
        loading = false
    }
}
