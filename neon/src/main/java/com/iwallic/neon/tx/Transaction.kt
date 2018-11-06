package com.iwallic.neon.tx

import com.iwallic.neon.utils.HEX

class Transaction {
    private var type: Int = TxTypeContract
    private var version: Int = TxVersionContract
    private var gas: Double = 0.0
    private val inputs: ArrayList<Input> = arrayListOf()
    private val outputs: ArrayList<Output> = arrayListOf()
    private val scripts: ArrayList<Script> = arrayListOf()
    private val attributes: ArrayList<Attribute> = arrayListOf()
    private val claims: ArrayList<Input> = arrayListOf()
    private var script: String? = null

    private var result: String = ""
    fun hash(): String {
        return HEX.hash256(serialize())
    }
    fun serialize(signed: Boolean = false): String {
        result = ""
        result += HEX.fromInt(type.toLong(), 1)
        result += HEX.fromInt(version.toLong(), 1)
        result += resolveType()
        result += resolveAttrs()
        result += resolveInputs()
        result += resolveOutputs()
        if (signed && scripts.isNotEmpty()) {
            result += HEX.fromVarInt(scripts.size.toLong())
            for (sc in scripts) {
                result += HEX.fromVarInt((sc.invocation.length/2).toLong()) + sc.invocation +
                        HEX.fromVarInt((sc.verification.length/2).toLong()) + sc.verification
            }
        }
        return result
    }
    fun sign(wif: String) {
        throw Throwable("Lib not complete")
//        val sign = "40" + Wallet.signature(serialize(), wif)
//        val verify = "21" + Wallet.priv2Pub(Wallet.wif2Priv(wif)) + "ac"
//        scripts.add(Script(sign, verify))
    }
    fun remark(data: String) {
        attributes.add(Attribute(AttrUsageRemark, HEX.fromString(data)))
    }
    private fun resolveType(): String {
        when (type) {
            TxTypeClaim -> {
                var rs = HEX.fromVarInt(claims.size.toLong())
                for (claim in claims) {
                    rs += HEX.reverse(claim.hash) + HEX.reverse(HEX.fromInt(claim.index.toLong(), 2))
                }
                return rs
            }
            TxTypeInvocation -> {
                if (script.isNullOrEmpty()) {
                    return ""
                }
                var rs: String = HEX.fromVarInt((script!!.length/2).toLong())
                rs += script
                if (version >= 1) {
                    rs += HEX.toFixedNum(gas, 8)
                }
                return rs
            }
            else -> {
                return ""
            }
        }
    }
    private fun resolveAttrs(): String {
        var rs = HEX.fromVarInt(attributes.size.toLong())
        for (attr in attributes) {
            if (attr.data.length > 65535) {
                return "00"
            }
            rs += HEX.fromInt(attr.usage.toLong(), 1, false)
            when {
                attr.usage == 0x81 -> {
                    rs += HEX.fromInt((attr.data.length/2).toLong(), 1)
                    rs += attr.data
                }
                attr.usage == 0x90 || attr.usage > 0xf0 -> {
                    rs += HEX.fromVarInt((attr.data.length/2).toLong())
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
        var rs: String = HEX.fromVarInt(inputs.size.toLong())
        for (input in inputs) {
            val tx = if (input.hash.startsWith("0x")) input.hash.substring(2) else input.hash
            rs += HEX.reverse(tx) + HEX.reverse(HEX.fromInt(input.index.toLong(), 2))
        }
        return rs
    }
    private fun resolveOutputs(): String {
        var rs: String = HEX.fromVarInt(outputs.size.toLong())
        for (output in outputs) {
            val asset = if (output.asset.startsWith("0x")) output.asset.substring(2) else output.asset
            val value = HEX.toFixedNum(output.value,8)
            val hash = if (output.scriptHash.startsWith("0x")) output.scriptHash.substring(2) else output.scriptHash
            rs += HEX.reverse(asset) + value + hash
        }
        return rs
    }
}
