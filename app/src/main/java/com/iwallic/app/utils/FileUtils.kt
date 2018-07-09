package com.iwallic.app.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.PrintWriter

object FileUtils {
    fun readWalletFile(context: Context, name: String): String? {
        var walletFile: File? = null
        try {
            walletFile = File(context.filesDir, name)
        } catch (e: Throwable) {
            Log.i("IWALLIC-FILE-READWALLET", e.toString())
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
            Log.i("IWALLIC-FILE-SAVEWALLET", e.toString())
            return false
        }
        return true
    }
}