package com.iwallic.app.utils

import android.content.Context
import com.google.gson.Gson
import com.iwallic.app.models.AccountModel
import com.iwallic.app.models.ContractModel
import com.iwallic.app.models.WalletAgentModel
import com.iwallic.app.models.WalletModel
import com.yitimo.neon.wallet.Wallet

object WalletUtils {
    private var cached: WalletModel? = null
    private var gson = Gson()
    /**
     * Create a wallet
     * 1. generate private key
     * 2. encrypt by the given password
     * 3. parse to NEP-6 wallet
     */
    fun create(pwd: String, privateKey: String? = null): WalletModel? {
        var pKey = privateKey
        if (pKey == null) {
            pKey = Wallet.generate()
        }
        val wif = Wallet.priv2Wif(pKey)
        val key = Wallet.neP2Encode(wif, pwd)
        val pub = Wallet.priv2Pub(pKey)
        val address = Wallet.priv2Addr(pKey)
        return WalletModel(accounts = arrayListOf(AccountModel(address = address, key = key, contract = ContractModel(script = pub))))
    }

    /**
     * Import a wallet
     * 1. if path then pick the file content
     * 2. parse to JSON
     * 3. parse to NEP-6 wallet
     */
    fun import (content: String, isPath: Boolean = false): WalletModel? {
        if (isPath) {
            return null
        }
        val w = gson.fromJson(content, WalletModel::class.java)
        if (w != null) {
            // try others
            return w
        }
        return null
    }

    /**
     * Import a wif
     * 1. encode to key
     * 2. parse to NEP-6 wallet
     */
    fun import (wif: String, pwd: String): WalletModel? {
        val key = Wallet.neP2Encode(wif, pwd)
        val pub = Wallet.priv2Pub(Wallet.wif2Priv(wif))
        val address = Wallet.pub2Addr(pub)
        return WalletModel(accounts = arrayListOf(AccountModel(address = address, key = key, contract = ContractModel(script = pub))))
    }

    /**
     * Save a wallet
     * 1. pick file name&snapshot
     * 2. save file
     * 3. save to SQLite
     * 4. set to SharedPreference
     */
    fun save (context: Context, wallet: WalletModel): Boolean {
        val newFile = (System.currentTimeMillis()/1000).toString()
        var snapshot = newFile
        var defaultAddress: String = ""
        if (wallet.accounts.isNotEmpty()) {
            snapshot = wallet.accounts[0].address
            defaultAddress = snapshot
        }
        if (!FileUtils.saveWalletFile(context, newFile, gson.toJson(wallet))) {
            return false
        }
        val newId = WalletDBUtils(context).add(newFile, snapshot)
        if (newId == null || newId < 1) {
            return false
        }
        SharedPrefUtils.setWallet(context, newId)
        SharedPrefUtils.setAddress(context, defaultAddress)
        return true
    }

    fun address(context: Context): String {
        // if address empty, check if wallet exists, if not then jump to gate
        return SharedPrefUtils.getAddress(context)
    }

    fun account(context: Context): AccountModel? {
        val w = wallet(context)
        if (w != null) {
            val address = SharedPrefUtils.getAddress(context)
            if (address.isNotEmpty() && w.accounts.isNotEmpty()) {
                return w.accounts.find {it.address == address}
            }
        }
        return null
    }

    fun wallet(context: Context): WalletModel? {
        if (cached != null) {
            return cached
        }
        val id = SharedPrefUtils.getWallet(context)
        if (id < 0) {
            return null
        }
        val wa: WalletAgentModel? = WalletDBUtils(context).get(id)
        if (wa == null) {
            return wa
        }
        val wString = FileUtils.readWalletFile(context, wa.file)
        if (wString != null) {
            val w = gson.fromJson(wString, WalletModel::class.java)
            if (w != null) {
                cached = w
                return w
            }
        }
        return null
    }

    fun check(str: String, type: String): Boolean {
        return when (type) {
            "wif" -> str.length == 52
            "private" -> str.length == 64
            "public" -> str.length == 66
            "address" -> str.length == 34
            "script" -> if (str.startsWith("0x")) str.length == 42 else str.length == 40
            "asset" -> if (str.startsWith("0x")) str.length == 66 else str.length == 64
            else -> false
        }
    }

    fun close(context: Context): Boolean {
        SharedPrefUtils.rmAddress(context)
        cached = null
        return true
    }
}