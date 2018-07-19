package com.iwallic.app.utils

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.iwallic.app.models.ResponsePyModel
import io.reactivex.Observable

object HttpClient {
    private val gson = Gson()

    fun post(method: String, params: List<Any> = emptyList(), ok: (res: String) -> Unit, no: (err: Int) -> Unit) {
        Fuel.post(ConfigUtils.api())
            .body(gson.toJson(RequestGoModel(method, params)))
            .responseString { _, _, result ->
                result.fold({ d ->
                    val rs = gson.fromJson(d, ResponseGoModel::class.java)
                    when {
                        rs == null -> no(99998)
                        rs.code == 200 -> {
                            Log.i("【request】", "complete【${method}】")
                            ok(gson.toJson(rs.result))
                        }
                        rs.code == 1000 -> {
                            ok("")
                        }
                        else -> {
                            Log.i("【request】", "error【${rs.msg}】")
                            no(rs.code)
                        }
                    }
                }) { err ->
                    Log.i("【request】", "error【${err}】")
                    no(resolveError(err.response.statusCode))
                }
            }
    }

    fun postPy(url: String, data: Map<String, Any>): Observable<String> {
        return Observable.create {
            Fuel.post("${ConfigUtils.apiDomain}$url")
                    .header(Pair("app_version", ConfigUtils.version))
                    .body(gson.toJson(data))
                    .responseString { _, _, result ->
                        result.fold({ d ->
                            it.onNext(d)
                            it.onComplete()
                        }) { err ->
                            Log.i("【request】", "error【${err}】")
                            it.onError(Throwable(resolveError(err.response.statusCode).toString()))
                        }
                    }
        }
    }

    fun getPy(url: String, ok: (String) -> Unit, no: (Int) -> Unit) {
        Fuel.get("${ConfigUtils.apiDomain}$url")
            .header(Pair("app_version", ConfigUtils.version), Pair("network", ConfigUtils.net))
            .responseString { _, _, result ->
                result.fold({ d ->
                    Log.i("【request】", "complete【$url】【$d】")
                    val rs = gson.fromJson(d, ResponsePyModel::class.java)
                    if (rs == null) {
                        no(99998)
                        return@fold
                    }
                    if (rs.bool_status) {
                        ok(gson.toJson(rs.data))
                    } else {
                        no(rs.error_code ?: 99999)
                    }
                }) { err ->
                    Log.i("【request】", "error【${err}】")
                    no(resolveError(err.response.statusCode))
                }
            }
    }

    private fun resolveError(status: Int): Int {
        return when (status) {
            400 -> 99997
            404 -> 99996
            500, 501, 502,  503 -> 99995
            -1 -> 99994
            else -> 99999
        }
    }
}

data class RequestGoModel (
    val method: String,
    val params: List<Any>
)

data class ResponseGoModel (
    val code: Int,
    val msg: String,
    val result: Any?
)
