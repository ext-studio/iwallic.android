package com.iwallic.app.models

import com.yitimo.neon.utils.Utils
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
        return Utils.reverseHex(Utils.toHash256(serialize()))
    }
    fun serialize(signed: Boolean = false): String {
        result = ""
        result += Utils.int2Hex(type.toLong(), 1, false)
        result += Utils.int2Hex(version.toLong(), 1, false)
        result += resolveType()
        result += resolveAttrs()
        result += resolveInputs()
        result += resolveOutputs()
        if (signed && scripts.isNotEmpty()) {
            result += Utils.int2VarInt(scripts.size.toLong())
            for (sc in scripts) {
                result += Utils.int2VarInt((sc.invocation.length/2).toLong()) + sc.invocation +
                        Utils.int2VarInt((sc.verification.length/2).toLong()) + sc.verification
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
        attributes.add(AttributeModel(AttrUsageRemark, Utils.string2Hex(data)))
    }
    private fun resolveType(): String {
        when (type) {
            TxTypeClaim -> {
                var rs = Utils.int2VarInt(claims.size.toLong())
                for (claim in claims) {
                    rs += Utils.reverseHex(claim.hash) + Utils.reverseHex(Utils.int2Hex(claim.index.toLong(), 2.toLong(), false))
                }
                return rs
            }
            TxTypeInvocation -> {
                if (script.isEmpty()) {
                    return ""
                }
                var rs: String = Utils.int2VarInt((script.length/2).toLong())
                rs += script
                if (version >= 1) {
                    val hex = Utils.int2VarInt((gas * 100000000).toLong())
                    rs += Utils.reverseHex("0".repeat(16 - hex.length)) + hex
                }
                return rs
            }
            else -> {
                return ""
            }
        }
    }
    private fun resolveAttrs(): String {
        var rs = Utils.int2VarInt(attributes.size.toLong())
        for (attr in attributes) {
            if (attr.data.length > 65535) {
                return "00"
            }
            rs += Utils.int2Hex(attr.usage.toLong(), 1.toLong(), false)
            when {
                attr.usage == 0x81 -> {
                    rs += Utils.int2Hex((attr.data.length/2).toLong(), 1.toLong(), false)
                    rs += attr.data
                }
                attr.usage == 0x90 || attr.usage > 0xf0 -> {
                    rs += Utils.int2VarInt((attr.data.length/2).toLong())
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
        var rs: String = Utils.int2VarInt(inputs.size.toLong())
        for (input in inputs) {
            rs += Utils.reverseHex(input.hash) + Utils.reverseHex(Utils.int2Hex(input.index.toLong(), 2.toLong(), false))
        }
        return rs
    }
    private fun resolveOutputs(): String {
        var rs: String = Utils.int2VarInt(outputs.size.toLong())
        for (output in outputs) {
            val hex = Utils.int2VarInt((output.value * 100000000).toLong())
            val value = Utils.reverseHex("0".repeat(16 - hex.length)) + hex
            rs += Utils.reverseHex(output.asset) + value + Utils.reverseHex(output.scriptHash)
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
