package com.iwallic.app.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iwallic.app.models.*
import com.iwallic.neon.wallet.Wallet
import io.reactivex.Observable
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

object NeonUtils {
    private var cachedAddress: String? = null
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
        var defaultAddress = ""
        if (wallet.accounts.isNotEmpty()) {
            snapshot = wallet.accounts[0].address
            defaultAddress = snapshot
        }
        if (!FileUtils.saveWalletFile(context, newFile, gson.toJson(wallet))) {
            return false
        }
        val newId = WalletDBUtils(context).add(newFile, defaultAddress, snapshot)
        if (newId == null || newId < 1) {
            return false
        }
        SharedPrefUtils.setWallet(context, newId)
        SharedPrefUtils.setAddress(context, defaultAddress)
        return true
    }

    /**
     * Switch to another exists wallet
     * 1. get wallet file
     * 2. verify pwd
     * 3. set to SharedPreference
     */
    fun switch(context: Context, agent: WalletAgentModel, pwd: String): Observable<Int> {
        return Observable.create {
            launch {
                val content = FileUtils.readWalletFile(context, agent.file)
                if (content.isNullOrEmpty()) {
                    withContext(UI) {
                        it.onNext(99598)
                    }
                    it.onComplete()
                    return@launch
                }
                val w = gson.fromJson(content, WalletModel::class.java)
                if (w == null) {
                    withContext(UI) {
                        it.onNext(99998)
                    }
                    it.onComplete()
                    return@launch
                }
                val a = w.accounts.find {am ->
                    am.address == agent.address
                }
                if (a == null) {
                    withContext(UI) {
                        it.onNext(99597)
                    }
                    it.onComplete()
                    return@launch
                }
                val check = Wallet.neP2Decode(a.key, pwd)
                if (check.isEmpty()) {
                    withContext(UI) {
                        it.onNext(99599)
                    }
                    it.onComplete()
                    return@launch
                }
                SharedPrefUtils.setWallet(context, agent._ID)
                SharedPrefUtils.setAddress(context, agent.address)
                WalletDBUtils(context).touch(agent._ID)
                withContext(UI) {
                    it.onNext(0)
                }
                it.onComplete()
                return@launch
            }
        }
    }

    /**
     * Remove wallet and file forever
     * 1. remove sql
     * 2. remove file
     */
    fun remove(context: Context, agent: WalletAgentModel) {
        WalletDBUtils(context).remove(agent._ID)
        FileUtils.rmWalletFile(context, agent.file)
    }

    fun address(context: Context): String {
        // if address empty, check if wallet exists, if not then jump to gate
        if (!cachedAddress.isNullOrEmpty()) {
            return cachedAddress!!
        }
        val addr = SharedPrefUtils.getAddress(context)
        if (!addr.isEmpty()) {
            cachedAddress = addr
        }
        return addr
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
        cachedAddress = null
        return true
    }

    fun verify(context: Context, pwd: String, ok: (String) -> Unit, no: (Int) -> Unit) {
        try {
            launch {
                val account = account(context)
                if (account == null) {
                    withContext(UI) {
                        no(99999)
                    }
                } else {
                    val rs = Wallet.neP2Decode(account.key, pwd)
                    withContext(UI) {
                        ok(rs)
                    }
                }
            }
        } catch (_: Throwable) {
            no(99999)
        }
    }

    fun fetchBalance(context: Context, address: String, assetId: String, ok: (ArrayList<UtxoModel>) -> Unit, no: (Int) -> Unit) {
        HttpUtils.post(context, "getutxoes", listOf(address, assetId), { res ->
            val data = try {gson.fromJson<ArrayList<UtxoModel>>(res, object : TypeToken<ArrayList<UtxoModel>>() {}.type)} catch (_: Throwable) {null}
            if (data == null) {
                no(99998)
            } else {
                ok(data)
            }
        }, {
            no(it)
        })
    }
}