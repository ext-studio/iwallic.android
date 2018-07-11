package com.iwallic.app.utils

import java.util.*
import android.content.Context

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
            return when (context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getInt("language", 0)) {
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
    }
}
