package com.iwallic.app.utils

import java.util.*
import android.content.Context
import com.iwallic.app.models.AssetRes
import kotlin.collections.ArrayList

class SharedPrefUtils {

    companion object {

        private const val PREF_APP = "com.iwallic.app.prefs"

        fun setWallet(context: Context, walletId: Long) {
            context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putLong("chosen_wallet", walletId).apply()
        }
        fun setAddress(context: Context, address: String) {
            context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putString("chosen_address", address).apply()
        }
        fun getWallet(context: Context): Long {
            return context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getLong("chosen_wallet", 0)
        }
        fun getAddress(context: Context): String {
            return context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getString("chosen_address", "")
        }
        fun setLocale(context: Context, locale: Locale) {
            context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putInt("language", when(locale.toString()) {
                "zh_CN" -> 1
                "en" -> 0
                else -> 0
            }).apply()
        }
        fun getLocale(context: Context): Locale {
            return when (context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getInt("language", -1)) {
                -1 -> {
                    // if pick system default filt unsupported some to English
                    resolveLocale(Locale.getDefault())
                }
                0 -> Locale.ENGLISH
                1 -> Locale.SIMPLIFIED_CHINESE
                else -> Locale.ENGLISH
            }
        }
        fun setSkin(context: Context, skin: String) {
            context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putString("skin", skin).apply()
        }
        fun getSkin(context: Context): String {
            return context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getString("skin", "default")
        }
        fun rmAddress(context: Context) {
            context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().remove("chosen_wallet").remove("chosen_address").apply()
        }
        fun addAsset(context: Context, data: AssetRes) {
            val tryGet = context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getStringSet("observer_asset", emptySet()).toMutableSet()
            tryGet.add(arrayOf(data.asset_id, "0", data.name, data.symbol).joinToString(","))
            context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putStringSet("observer_asset", tryGet).apply()
        }
        fun getAsset(context: Context): ArrayList<AssetRes> {
            val rs = arrayListOf<AssetRes>()
            try {
                val tryGet = context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getStringSet("observer_asset", emptySet())
                tryGet.forEach {
                    val sp = it.split(",")
                    rs.add(AssetRes(sp[0], sp[1], sp[2], sp[3]))
                }
            } catch (_: Throwable) {
                context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().remove("observer_asset").apply()
            }
            return rs
        }
        fun rmAsset(context: Context, assetId: String) {
            val tryGet = context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getStringSet("observer_asset", setOf())
            val newList = tryGet.filter {
                !it.contains(assetId)
            }
            context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putStringSet("observer_asset", newList.toSet()).apply()
        }
        fun rmAsset(context: Context, assetIds: ArrayList<String>) {
            val tryGet = context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getStringSet("observer_asset", setOf())
            val newList = tryGet.filter {
                assetIds.all {
                    !it.contains(it)
                }
            }
            context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putStringSet("observer_asset", newList.toSet()).apply()
        }
        fun getNet(context: Context): String {
            return context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getString("net", "main")
        }
        fun setNet(context: Context, net: String) {
            context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putString("net", if (net == "main") "main" else "test").apply()
        }

        private fun resolveLocale(default: Locale): Locale {
            return when (default) {
                Locale.CHINESE, Locale.SIMPLIFIED_CHINESE, Locale.TRADITIONAL_CHINESE, Locale.CHINA -> Locale.SIMPLIFIED_CHINESE
                else -> Locale.ENGLISH
            }
        }
    }
}
