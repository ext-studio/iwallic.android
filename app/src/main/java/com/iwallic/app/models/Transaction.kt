package com.iwallic.app.models

import com.yitimo.neon.transaction.Script
import com.yitimo.neon.transaction.Transaction
import com.yitimo.neon.utils.Utils
import com.yitimo.neon.wallet.Wallet

const val ASSET_GAS = ""

enum class TxVersion(v: Int) {
    CLAIM(0),
    CONTRACT(0),
    INVOCATION(1),
}

enum class TxType(v: Int) {
    CLAIM(2),
    CONTRACT(128),
    INVOCATION(209),
}

enum class AttrUsage(v: Int) {
    SCRIPT(0x20),
    REMARK(0xf0),
    NEP5(0xf1),
}

data class InputModel(
    val hash: String,
    val index: Int
)

data class OutputModel(
    val asset: String,
    val scriptHash: String,
    val value: Float
)

data class ScriptModel(
    val invocation: String,
    val verification: String
)

data class AttributeModel(
    val usage: AttrUsage,
    val data: String
)

data class UtxoModel(
    val index: Int,
    val hash: String,
    val value: Float,
    val asset: String
)

data class ClaimModel(
    val txid: String,
    val index: Int
)

class TransactionModel {
    var type: TxType = TxType.CONTRACT
    var version: TxVersion = TxVersion.CONTRACT
    var claims: ArrayList<InputModel> = arrayListOf()
    var attributes: ArrayList<AttributeModel> = arrayListOf()
    var gas: Float = 0f
    var script: String? = null
    var scripts: ArrayList<ScriptModel> = arrayListOf()
    var inputs: ArrayList<InputModel> = arrayListOf()
    var outputs: ArrayList<OutputModel> = arrayListOf()
    fun hash(): String {
        // todo need implement
        // 对序列化结果进行hash
        return ""
    }
    fun serialize(): String {
        // todo need implement
        // 序列化交易
        return ""
    }
    fun sign(wif: String) {
        val sign = "40" + Wallet.signature(hash(), wif)
        val verify = "21" + Wallet.priv2Pub(Wallet.wif2Priv(wif)) + "ac"
        scripts.add(ScriptModel(sign, verify))
    }
    fun remark(data: String) {
        attributes.add(AttributeModel(AttrUsage.REMARK, Utils.string2Hex(data)))
    }
    companion object {
        fun forAsset(utxo: ArrayList<UtxoModel>, from: String, to: String, amount: Float, asset: String): TransactionModel? {
            val fromScript = Wallet.addr2Script(from)
            val toScript = Wallet.addr2Script(to)
            val newTX = TransactionModel()
            newTX.outputs.add(OutputModel(asset, toScript, amount))
            var curr = 0f
            for (tx in utxo) {
                curr += tx.value
                newTX.inputs.add(InputModel(tx.hash, tx.index))
                if (curr >= amount) {
                    break
                }
            }
            val payback = curr - amount
            if (payback < 0) {
                return null
            }
            if (payback > 0) {
                newTX.outputs.add(OutputModel(asset, fromScript, payback))
            }
            return newTX
        }
        fun forToken(token: String, from: String, to: String, amount: Float): TransactionModel? {
            val newTX = TransactionModel()
            newTX.type = TxType.INVOCATION
            newTX.version = TxVersion.INVOCATION
            // todo need implement
            newTX.script = ""
            newTX.attributes.add(AttributeModel(AttrUsage.NEP5, ""))
            return null
        }
        fun forClaim(claims: ArrayList<InputModel>, value: Float, to: String): TransactionModel? {
            val newTX = TransactionModel()
            newTX.type = TxType.CLAIM
            newTX.version = TxVersion.CLAIM
            newTX.claims = claims
            newTX.outputs.add(OutputModel(ASSET_GAS, Wallet.addr2Script(to), value))
            return newTX
        }
    }
}
