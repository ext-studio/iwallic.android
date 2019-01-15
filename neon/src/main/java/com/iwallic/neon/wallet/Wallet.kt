package com.iwallic.neon.wallet

import org.json.JSONObject

class Wallet {
    var accounts: ArrayList<Account> = arrayListOf()
    fun verify(pwd: String, ok: (String) -> Unit, no: (Int) -> Unit) {
        throw Throwable("completing")
    }
    fun toNep6(): JSONObject {
        throw Throwable("completing")
    }
    companion object {
        fun generateWIF(): String {
            throw Throwable("completing")
        }
        fun generate(pwd: String): Wallet {
            throw Throwable("completing")
        }
    }
}
