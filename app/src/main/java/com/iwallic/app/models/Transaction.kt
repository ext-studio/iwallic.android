package com.iwallic.app.models

import com.google.gson.annotations.SerializedName
import com.iwallic.app.utils.CommonUtils
import com.iwallic.neon.hex.Hex
import com.iwallic.neon.wallet.Wallet

const val TxVersionClaim = 0
const val TxVersionContract = 0
const val TxVersionInvocation = 1

const val TxTypeClaim = 2
const val TxTypeContract = 128
const val TxTypeInvocation = 209

const val AttrUsageScript = 0x20
const val AttrUsageRemark = 0xf0
const val AttrUsageNep5 = 0xf1

data class InputModel(
    val hash: String,
    val index: Int
)

data class OutputModel(
    val asset: String,
    val scriptHash: String,
    val value: Double
)

data class ScriptModel(
    val invocation: String,
    val verification: String
)

data class AttributeModel(
    val usage: Int,
    val data: String
)

data class UtxoModel(
    @SerializedName("n") val index: Int,
    @SerializedName("txid") val hash: String,
    @SerializedName("value") val value: Double,
    @SerializedName("assetId") val asset: String
)

class TransactionModel {
    var type: Int = TxTypeContract
    var version: Int = TxVersionContract
    var claims: List<InputModel> = listOf()
    var attributes: ArrayList<AttributeModel> = arrayListOf()
    var gas: Double = 0.toDouble()
    var script: String = ""
    var scripts: ArrayList<ScriptModel> = arrayListOf()
    var inputs: ArrayList<InputModel> = arrayListOf()
    var outputs: ArrayList<OutputModel> = arrayListOf()
    private var result: String = ""
    fun hash(): String {
        return Hex.hash256(serialize())
    }
    fun serialize(signed: Boolean = false): String {
        result = ""
        result += Hex.fromInt(type.toLong(), 1, false)
        result += Hex.fromInt(version.toLong(), 1, false)
        result += resolveType()
        result += resolveAttrs()
        result += resolveInputs()
        result += resolveOutputs()
        if (signed && scripts.isNotEmpty()) {
            result += Hex.fromVarInt(scripts.size.toLong())
            for (sc in scripts) {
                result += Hex.fromVarInt((sc.invocation.length/2).toLong()) + sc.invocation +
                        Hex.fromVarInt((sc.verification.length/2).toLong()) + sc.verification
            }
        }
        return result
    }
    fun sign(wif: String): Boolean {
        return try {
            val sign = "40" + Wallet.signature(serialize(), wif)
            val verify = "21" + Wallet.priv2Pub(Wallet.wif2Priv(wif)) + "ac"
            scripts.add(ScriptModel(sign, verify))
            true
        } catch(_: Throwable) {
            false
        }
    }
    fun remark(data: String) {
        attributes.add(AttributeModel(AttrUsageRemark, Hex.fromString(data)))
    }
    private fun resolveType(): String {
        when (type) {
            TxTypeClaim -> {
                var rs = Hex.fromVarInt(claims.size.toLong())
                for (claim in claims) {
                    rs += Hex.reverse(claim.hash) + Hex.reverse(Hex.fromInt(claim.index.toLong(), 2.toLong(), false))
                }
                return rs
            }
            TxTypeInvocation -> {
                if (script.isEmpty()) {
                    return ""
                }
                var rs: String = Hex.fromVarInt((script.length/2).toLong())
                rs += script
                if (version >= 1) {
                    rs += Hex.toFixedNum(gas, 8)
                }
                return rs
            }
            else -> {
                return ""
            }
        }
    }
    private fun resolveAttrs(): String {
        var rs = Hex.fromVarInt(attributes.size.toLong())
        for (attr in attributes) {
            if (attr.data.length > 65535) {
                return "00"
            }
            rs += Hex.fromInt(attr.usage.toLong(), 1, false)
            when {
                attr.usage == 0x81 -> {
                    rs += Hex.fromInt((attr.data.length/2).toLong(), 1.toLong(), false)
                    rs += attr.data
                }
                attr.usage == 0x90 || attr.usage > 0xf0 -> {
                    rs += Hex.fromVarInt((attr.data.length/2).toLong())
                    rs += attr.data
                }
                attr.usage == 0x02 || attr.usage == 0x03 -> {
                    rs += attr.data.substring(2, 64)
                }
                else -> {
                    rs += attr.data
                }
            }
        }
        return rs
    }
    private fun resolveInputs(): String {
        var rs: String = Hex.fromVarInt(inputs.size.toLong())
        for (input in inputs) {
            val tx = if (input.hash.startsWith("0x")) input.hash.substring(2) else input.hash
            rs += Hex.reverse(tx) + Hex.reverse(Hex.fromInt(input.index.toLong(), 2.toLong(), false))
        }
        return rs
    }
    private fun resolveOutputs(): String {
        var rs: String = Hex.fromVarInt(outputs.size.toLong())
        for (output in outputs) {
            val asset = if (output.asset.startsWith("0x")) output.asset.substring(2) else output.asset
            val value = Hex.toFixedNum(output.value,8)
            val hash = if (output.scriptHash.startsWith("0x")) output.scriptHash.substring(2) else output.scriptHash
            rs += Hex.reverse(asset) + value + hash
        }
        return rs
    }
    companion object {
        fun forAsset(utxo: ArrayList<UtxoModel>, from: String, to: String, amount: Double, asset: String): TransactionModel? {
            val fromScript = Wallet.addr2Script(from)
            val toScript = Wallet.addr2Script(to)
            if (fromScript.length != 40 || toScript.length != 40) {
                return null
            }
            val newTX = TransactionModel()
            newTX.outputs.add(OutputModel(asset, toScript, amount))
            var curr = 0.0
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
        fun forToken(token: String, from: String, to: String, amount: Double): TransactionModel? {
            val newTx = TransactionModel()
            newTx.type = TxTypeInvocation
            newTx.version = TxVersionInvocation
            val contract = SmartContract.forNEP5(token, from, to, amount) ?: return null
            newTx.script = contract.serielize() + "f1"
            newTx.attributes.add(AttributeModel(AttrUsageScript, Wallet.addr2Script(from)))
            newTx.attributes.add(AttributeModel(AttrUsageNep5, Hex.reverse(Hex.fromString("from iwallic at ${System.currentTimeMillis()/1000}"))))
            return newTx
        }
        fun forClaim(claims: ArrayList<ClaimItemRes>, value: Double, to: String): TransactionModel? {
            val newTX = TransactionModel()
            newTX.type = TxTypeClaim
            newTX.version = TxVersionClaim
            newTX.claims = claims.map {
                InputModel(it.txid, it.index.toInt())
            }
            newTX.outputs.add(OutputModel(CommonUtils.GAS, Wallet.addr2Script(to), value))
            return newTX
        }
    }
}
