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

    fun list2(context: Context?, address: String, force: Boolean = false): Observable<ArrayList<AssetRes>> {
        return Observable.create { observer ->
            if (cached != null && !force) {
                observer.onNext(cached!!)
                observer.onComplete()
                return@create
            }
            HttpUtils.getPy(context, "/client/index/assets/display?wallet_address=$address", {
                val data = try {gson.fromJson<ArrayList<AssetRes>>(it, object: TypeToken<ArrayList<AssetRes>>() {}.type)} catch (_: Throwable) {null}
                if (data == null) {
                    observer.onError(Throwable("99999"))
                } else {
                    cached = data
                    observer.onNext(data)
                    observer.onComplete()
                }
            }, {
                observer.onError(Throwable("$it"))
            })
        }
    }

    fun checkClaim(): Boolean {
        return try {cached?.find { it.asset_id == CommonUtils.NEO }?.balance?.toInt() ?: 0} catch (_: Throwable) {0} > 0
    }

    fun clear() {
        cached = null
    }
}
