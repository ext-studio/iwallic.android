package com.iwallic.app.utils

import android.content.Context
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.iwallic.app.BuildConfig
import com.iwallic.app.models.RequestGoModel
import com.iwallic.app.models.ResponseGoModel
import com.iwallic.app.models.ResponsePyModel

object HttpUtils {
    private val gson = Gson()

    fun post(context: Context?, method: String, params: List<Any> = emptyList(), ok: (res: String) -> Unit, no: (err: Int) -> Unit) {
        Fuel.post(CommonUtils.neonApi)
            .timeout(30000)
            .timeoutRead(30000)
            .body(gson.toJson(RequestGoModel(method, params)))
            .responseString { _, _, result ->
                result.fold({ d ->
                    val rs = try {gson.fromJson(d, ResponseGoModel::class.java)} catch (_: Throwable) {null}
                    Log.i("【PostGo】", "$method -> $rs")
                    when {
                        rs == null -> no(99998)
                        rs.code == 200 -> {
                            ok(gson.toJson(rs.result))
                        }
                        rs.code == 1000 -> {
                            ok("null")
                        }
                        else -> {
                            no(rs.code)
                        }
                    }
                }) { err ->
                    no(resolveError(err.response.statusCode))
                }
            }
    }

    fun postPy(context: Context?, url: String, data: Map<String, Any>, ok: (String) -> Unit, no: (Int) -> Unit) {
        Fuel.post("${CommonUtils.pyApi}$url")
            .header(
                Pair("app_version", BuildConfig.VERSION_NAME),
                Pair("network", SharedPrefUtils.getNet(context)),
                Pair("Content-Type", "application/json")
            )
            .timeout(30000)
            .timeoutRead(30000)
            .body(gson.toJson(data))
            .responseString { _, _, result ->
                result.fold({ d ->
                    val rs = try {gson.fromJson(d, ResponsePyModel::class.java)} catch (_: Throwable) {null}
                    if (rs == null) {
                        no(99998)
                        return@fold
                    }
                    Log.i("【Post】", "$url -> $rs")
                    if (rs.bool_status) {
                        ok(gson.toJson(rs.data))
                    } else {
                        no(rs.error_code ?: 99999)
                    }
                }) { err ->
                    no(resolveError(err.response.statusCode))
                }
            }
    }

    fun putPy(context: Context?, url: String, data: Map<String, Any>, ok: (String) -> Unit, no: (Int) -> Unit) {
        Fuel.put("${CommonUtils.pyApi}$url")
            .header(
                Pair("app_version", BuildConfig.VERSION_NAME),
                Pair("network", SharedPrefUtils.getNet(context)),
                Pair("Content-Type", "application/json")
            ).body(gson.toJson(data))
            .responseString { _, _, result ->
                result.fold({ d ->
                    val rs = try {gson.fromJson(d, ResponsePyModel::class.java)} catch (_: Throwable) {null}
                    if (rs == null) {
                        no(99998)
                        return@fold
                    }
                    Log.i("【Put】", "$url -> $rs")
                    if (rs.bool_status) {
                        ok(gson.toJson(rs.data))
                    } else {
                        no(rs.error_code ?: 99999)
                    }
                }) { err ->
                    no(resolveError(err.response.statusCode))
                }
            }
    }

    fun getPy(context: Context?, url: String, ok: (String) -> Unit, no: (Int) -> Unit) {
        Fuel.get("${CommonUtils.pyApi}$url")
            .header(
                Pair("app_version", BuildConfig.VERSION_NAME),
                Pair("network", SharedPrefUtils.getNet(context))
            ).responseString { _, _, result ->
                result.fold({ d ->
                    val rs = try {gson.fromJson(d, ResponsePyModel::class.java)} catch (_: Throwable) {null}
                    if (rs == null) {
                        no(99998)
                        return@fold
                    }
                    Log.i("【Get】", "$url -> $rs")
                    if (rs.bool_status) {
                        ok(gson.toJson(rs.data))
                    } else {
                        no(rs.error_code ?: 99999)
                    }
                }) { err ->
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
