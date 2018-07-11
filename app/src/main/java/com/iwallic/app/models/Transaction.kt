package com.iwallic.app.models

import com.yitimo.neon.hex.Hex
import com.yitimo.neon.wallet.Wallet

const val ASSET_GAS = ""

const val TxVersionClaim = 0
const val TxVersionContract = 0
const val TxVersionInvocation = 0

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
    val value: Float
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
    var type: Int = TxTypeContract
    var version: Int = TxVersionContract
    var claims: ArrayList<InputModel> = arrayListOf()
    var attributes: ArrayList<AttributeModel> = arrayListOf()
    var gas: Float = 0f
    var script: String = ""
    var scripts: ArrayList<ScriptModel> = arrayListOf()
    var inputs: ArrayList<InputModel> = arrayListOf()
    var outputs: ArrayList<OutputModel> = arrayListOf()
    private var result: String = ""
    fun hash(): String {
        return  Hex.reverse(Hex.hash256(serialize()))
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
        // 序列化交易
        return result
    }
    fun sign(wif: String) {
        val sign = "40" + Wallet.signature(hash(), wif)
        val verify = "21" + Wallet.priv2Pub(Wallet.wif2Priv(wif)) + "ac"
        scripts.add(ScriptModel(sign, verify))
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
                    val hex = Hex.fromVarInt((gas * 100000000).toLong())
                    rs += Hex.reverse("0".repeat(16 - hex.length)) + hex
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
            rs += Hex.fromInt(attr.usage.toLong(), 1.toLong(), false)
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
            rs += Hex.reverse(input.hash) + Hex.reverse(Hex.fromInt(input.index.toLong(), 2.toLong(), false))
        }
        return rs
    }
    private fun resolveOutputs(): String {
        var rs: String = Hex.fromVarInt(outputs.size.toLong())
        for (output in outputs) {
            val hex = Hex.fromVarInt((output.value * 100000000).toLong())
            val value = Hex.reverse("0".repeat(16 - hex.length)) + hex
            rs += Hex.reverse(output.asset) + value + Hex.reverse(output.scriptHash)
        }
        return rs
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
            newTX.type = TxTypeInvocation
            newTX.version = TxVersionInvocation
            newTX.script = SmartContract.forNEP5(token, from, to, amount).serielize()
            newTX.attributes.add(AttributeModel(AttrUsageNep5, ""))
            return null
        }
        fun forClaim(claims: ArrayList<InputModel>, value: Float, to: String): TransactionModel? {
            val newTX = TransactionModel()
            newTX.type = TxTypeClaim
            newTX.version = TxVersionClaim
            newTX.claims = claims
            newTX.outputs.add(OutputModel(ASSET_GAS, Wallet.addr2Script(to), value))
            return newTX
        }
    }
}
