package com.iwallic.app.states

import android.content.Context
import com.google.gson.Gson
import com.iwallic.app.models.VersionRes
import com.iwallic.app.utils.ACache
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.HttpUtils
import io.reactivex.Observable

object VersionState {
    private val gson = Gson()
    fun check(context: Context, force: Boolean = false, ok: (VersionRes?) -> Unit, no: (Int) -> Unit) {
            val aCache = ACache.get(context)
            val local = aCache.getAsString("version") ?: ""
            if (local.isNotEmpty() && !force) {
                val rs = try {
                    gson.fromJson(local, VersionRes::class.java)
                } catch (_: Throwable) {
                    null
                }
                if (rs == null) {
                    aCache.remove("version")
                    no(99998)
                } else {
                    ok(rs)
                }
            } else {
                HttpUtils.getPy(context, "/client/index/app_version/detail", {
                    val info = try {gson.fromJson(it, VersionRes::class.java)} catch (_: Throwable) {null}
                    if (info != null) {
                        aCache.put("version", gson.toJson(info))
                        ok(info)
                    } else {
                        no(99998)
                    }
                }, {
                    ok(null)
                })
            }
    }

    fun tip(context: Context, info: VersionRes, ok: () -> Unit, no: () -> Unit) {
        DialogUtils.confirm(context, {
            if (it) {
                ok()
            } else {
                no()
            }
        }, (info.info["cn"] as String).replace("\\n", "\n"), "发现新版本", "现在更新", "暂时忽略")
    }

    fun force(context: Context?, info: VersionRes, ok: () -> Unit, no: () -> Unit) {
        DialogUtils.confirm(context!!, {
            if (it) {
                ok()
            } else {
                no()
            }
        }, (info.info["cn"] as String).replace("\\n", "\n"), "发现重要更新", "现在更新", "离开(不推荐)")
    }
}
