package com.iwallic.app.utils

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.iwallic.app.models.RequestGoModel
import com.iwallic.app.models.ResponseGoModel
import com.iwallic.app.models.ResponsePyModel
import io.reactivex.Observable
import java.net.HttpURLConnection
import java.net.URL

object HttpUtils {
    private val gson = Gson()

    fun post(method: String, params: List<Any> = emptyList(), ok: (res: String) -> Unit, no: (err: Int) -> Unit) {
        Fuel.post(CommonUtils.apiGo())
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

    fun postPy(url: String, data: Map<String, Any>, ok: (String) -> Unit, no: (Int) -> Unit) {
            Fuel.post("${CommonUtils.pyApi}$url")
                    .header(Pair("app_version", CommonUtils.versionName))
                    .body(gson.toJson(data))
                    .responseString { _, _, result ->
                        result.fold({ d ->
                            Log.i("【request】", "complete【post】【$url】")
                            Log.i("【request】", "$data === $d")
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

    fun putPy(url: String, data: Map<String, Any>, ok: (String) -> Unit, no: (Int) -> Unit) {
        Fuel.put("${CommonUtils.pyApi}$url")
            .header(Pair("app_version", CommonUtils.versionName))
            .body(gson.toJson(data))
            .responseString { _, _, result ->
                result.fold({ d ->
                    Log.i("【request】", "complete【put】【$url】")
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

    fun getPy(_url: String, ok: (String) -> Unit, no: (Int) -> Unit) {
        Fuel.get("${CommonUtils.pyApi}$_url")
            .header(Pair("app_version", CommonUtils.versionName), Pair("network", CommonUtils.net))
            .responseString { _, _, result ->
                result.fold({ d ->
                    Log.i("【request】", "【$result】")
                    Log.i("【request】", "complete【get】【$_url】")
                    val rs = gson.fromJson(d, ResponsePyModel::class.java)
                    if (rs == null) {
                        no(99998)
                        return@fold
                    }
                    when {
                        rs.bool_status -> ok(gson.toJson(rs.data))
                        rs.error_code == 200000 -> {
                            Log.i("【request】", "empty【$_url】【$rs】")
                            ok("[]")
                        }
                        else -> {
                            Log.i("【request】", "error【$_url】【${rs}】")
                            no(rs.error_code ?: 99999)
                        }
                    }
                }) { err ->
                    Log.i("【request】", "error【$_url】【${err}】")
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
