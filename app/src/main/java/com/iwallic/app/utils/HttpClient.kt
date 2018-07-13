package com.iwallic.app.utils

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import io.reactivex.Observable

object HttpClient {
    private fun apiDomain(net: String) = if (net == "main") "https://api.iwallic.com/api/iwallic" else "https://teapi.iwallic.com/api/iwallic"
    private val gson = Gson()
    @Suppress("UNCHECKED_CAST")
    fun post(method: String, params: List<Any> = emptyList(), ok: (res: String) -> Unit, no: (err: Int) -> Unit, net: String = "main") {
        Fuel.post(apiDomain(net))
            .body(gson.toJson(RequestModel(method, params)))
            .responseString { _, _, result ->
                result.fold({ d ->
                    val rs = gson.fromJson(d, ResponseModel::class.java)
                    when {
                        rs == null -> no(99998)
                        rs.code == 200 -> {
                            Log.i("网络请求", "完成【${method}】")
                            ok(gson.toJson(rs.result))
                        }
                        rs.code == 1000 -> {
                            ok("")
                        }
                        else -> {
                            Log.i("网络请求", "出错【${rs.msg}】")
                            no(rs.code)
                        }
                    }
                }) { err ->
                    Log.i("网络请求", "失败【${err}】")
                    no(resolveError(err.response.statusCode))
                }
            }
    }
    private fun resolveError(status: Int): Int {
        Log.i("网络请求", "返回状态码【$status】")
        return when (status) {
            400 -> 99997
            404 -> 99996
            500, 501, 502,  503 -> 99995
            -1 -> 99994
            else -> 99999
        }
    }
}

data class RequestModel (
    val method: String,
    val params: List<Any>
)

data class ResponseModel (
    val code: Int,
    val msg: String,
    val result: Any?
)

