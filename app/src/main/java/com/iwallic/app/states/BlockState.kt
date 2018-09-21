package com.iwallic.app.states

import android.content.Context
import com.google.gson.Gson
import com.iwallic.app.models.BlockTimeRes
import com.iwallic.app.utils.ACache
import com.iwallic.app.utils.HttpUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object BlockState {
    private var _current = BlockTimeRes()
    private val gson = Gson()

    fun fetch(context: Context?, ok: (BlockTimeRes, Boolean) -> Unit, no: (Int) -> Unit) {
        HttpUtils.post(context, "getblocktime", emptyList(), fun (res) {
            val blockData = try {gson.fromJson(res, BlockTimeRes::class.java)} catch (_: Throwable) {null}
            if (blockData == null) {
                no(99998)
            } else {
                if (blockData.lastBlockIndex > _current.lastBlockIndex) {
                    _current = blockData
                    ok(_current, true)
                } else {
                    ok(_current, false)
                }
            }
        }, fun (err) {
            no(err)
        })
    }

    fun current(context: Context?, ok: (BlockTimeRes, Boolean) -> Unit, no: (Int) -> Unit) {
        if (_current.lastBlockIndex == 0.toLong()) {
            fetch(context, ok, no)
        } else {
            ok(_current, false)
        }
    }
}
