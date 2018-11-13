package com.iwallic.app.utils

import java.util.*
import android.content.Context
import com.iwallic.app.models.AssetRes
import kotlin.collections.ArrayList

object SharedPrefUtils {

    private const val PREF_APP = "com.iwallic.app.prefs"

    fun getString(context: Context?, key: String): String {
        return context?.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE)?.getString(key, "") ?: ""
    }

    fun setWallet(context: Context, walletId: Long) {
        context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putLong("chosen_wallet", walletId).apply()
    }
    fun setAddress(context: Context, address: String) {
        context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putString("chosen_address", address).apply()
    }
    fun getWallet(context: Context): Long {
        return context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getLong("chosen_wallet", 0)
    }
    fun getAddress(context: Context?): String {
        return context?.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE)?.getString("chosen_address", "") ?: ""
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
        return context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getString("skin", "default") ?: "default"
    }
    fun rmAddress(context: Context) {
        val sp = context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit()
        sp.remove("chosen_wallet")
        sp.remove("chosen_address")
        sp.apply()
    }
    fun getNet(context: Context?): String {
        return "main"
//        return context?.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE)?.getString("chain_net", "main") ?: "main"
    }
    fun setNet(context: Context, net: String) {
        context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putString("chain_net", if (net == "main") "main" else "test").apply()
    }
    fun getClaim(context: Context): String {
        val rs = (context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getString("claim", ",0") ?: ",0").split(",")
        val txid = rs.getOrNull(0) ?: ""
        val time = rs.getOrNull(1)?.toLong() ?: 0.toLong()
        val expired = System.currentTimeMillis() / 1000 + 3600
        return if (time < expired) "" else txid
    }
    fun setClaim(context: Context, txid: String) {
        context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putString("claim", "$txid,${System.currentTimeMillis()/1000}").apply()
    }
    fun getCollect(context: Context): String {
        val rs = (context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getString("collect", ",0") ?: ",0").split(",")
        val txid = rs.getOrNull(0) ?: ""
        val time = rs.getOrNull(1)?.toLong() ?: 0.toLong()
        val expired = System.currentTimeMillis() / 1000 + 3600
        return if (time < expired) "" else txid
    }
    fun setCollect(context: Context, txid: String) {
        context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putString("collect", "$txid,${System.currentTimeMillis()/1000}").apply()
    }

    private fun resolveLocale(default: Locale): Locale {
        return when (default) {
            Locale.CHINESE, Locale.SIMPLIFIED_CHINESE, Locale.TRADITIONAL_CHINESE, Locale.CHINA -> Locale.SIMPLIFIED_CHINESE
            else -> Locale.ENGLISH
        }
    }
}
