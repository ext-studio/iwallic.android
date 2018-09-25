package com.iwallic.app.states

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.AssetRes
import com.iwallic.app.utils.CommonUtils
import com.iwallic.app.utils.HttpUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlin.collections.ArrayList

object AssetState {
    var cached: ArrayList<AssetRes>? = null
    private val gson = Gson()

    fun get(id: String): AssetRes? {
        return cached?.find { it.asset_id == id }
    }

    fun list(context: Context?, address: String, force: Boolean = false, ok: (ArrayList<AssetRes>) -> Unit, no: (Int) -> Unit) {
        if (cached != null && !force) {
            ok(cached!!)
            return
        }
        HttpUtils.getPy(context, "/client/index/assets/display?wallet_address=$address", {
            val data = try {gson.fromJson<ArrayList<AssetRes>>(it, object: TypeToken<ArrayList<AssetRes>>() {}.type)} catch (_: Throwable) {null}
            if (data == null) {
                no(99998)
            } else {
                cached = data
                ok(data)
            }
        }, {
            no(it)
        })
    }

    fun checkClaim(): Boolean {
        return try {cached?.find { it.asset_id == CommonUtils.NEO }?.balance?.toInt() ?: 0} catch (_: Throwable) {0} > 0
    }
}
