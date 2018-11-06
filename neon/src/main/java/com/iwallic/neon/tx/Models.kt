package com.iwallic.neon.tx

const val TxVersionClaim = 0
const val TxVersionContract = 0
const val TxVersionInvocation = 1

const val TxTypeClaim = 2
const val TxTypeContract = 128
const val TxTypeInvocation = 209

const val AttrUsageScript = 0x20
const val AttrUsageRemark = 0xf0
const val AttrUsageNep5 = 0xf1

data class Input (
    val hash: String,
    val index: Int
)

data class Output (
    val asset: String,
    val scriptHash: String,
    val value: Double
)

data class Script (
    val invocation: String,
    val verification: String
)

data class Attribute (
    val usage: Int,
    val data: String
)

data class UTXO (
    val index: Int,
    val hash: String,
    val value: Double,
    val asset: String
)