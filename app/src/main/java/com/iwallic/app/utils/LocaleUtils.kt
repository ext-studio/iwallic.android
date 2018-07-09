package com.iwallic.app.utils

import android.os.Build
import android.annotation.TargetApi
import android.content.Context
import android.preference.PreferenceManager
import java.util.*


object LocaleUtils {
    fun Current(context: Context): Locale {
        return SharedPrefUtils.getLocale(context)
    }

    fun OnAttach(context: Context): Context {
        val locale = SharedPrefUtils.getLocale(context)
        return this.SetLocale(context, locale)
    }

    fun SetLocale(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        SharedPrefUtils.setLocale(context, locale)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, locale)
        } else {
            updateResourcesLegacy(context, locale)
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, locale: Locale): Context {
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    @Suppress("DEPRECATION")
    private fun updateResourcesLegacy(context: Context, locale: Locale): Context {
        val resources = context.resources

        val configuration = resources.configuration
        configuration.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale)
        }

        resources.updateConfiguration(configuration, resources.displayMetrics)

        return context
    }
}