package com.iwallic.app.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.PrintWriter

object FileUtils {
    fun readWalletFile(context: Context, name: String): String? {
        val walletFile: File?
        try {
            walletFile = File(context.filesDir, name)
        } catch (e: Throwable) {
            Log.i("【FileUtil】", "read error【$e】")
            return null
        }
        if (!walletFile.exists()) {
            return null
        }
        return walletFile.readText()
    }
    fun saveWalletFile(context: Context, name: String, content: String): Boolean {
        try {
            context.openFileOutput(name, Context.MODE_PRIVATE).use {
                it.write(content.toByteArray())
            }
        } catch (e: Throwable) {
            Log.i("【FileUtil】", "write error【$e】")
            return false
        }
        return true
    }
    fun rmWalletFile(context: Context, name: String) {
        try {
            val walletFile = File(context.filesDir, name)
            walletFile.delete()
        } catch (e: Throwable) {
            Log.i("【FileUtil】", "delete error【$e】")
        }
    }
    fun cleanApkFile(context: Context) {
        try {
            File(context.filesDir, "installers").deleteOnExit()
            Log.i("【FileUtil】", "delete exist apk")
        } catch (e: Throwable) {
            Log.i("【FileUtil】", "delete error【$e】")
        }
    }
}