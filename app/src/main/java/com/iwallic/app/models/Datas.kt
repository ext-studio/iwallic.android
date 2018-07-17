package com.iwallic.app.models

data class BalanceRes (
    val assetId: String,
    val balance: String,
    val name: String,
    val symbol: String
)

data class PageDataRes<T>(
    var page: Int = 1,
    var pageSize: Int = 10,
    var total: Int = 0,
    var data: ArrayList<T> = arrayListOf()
)

data class TransactionRes (
    val name: String,
    val txid: String,
    val value: String
)

data class BlockTimeRes (
    val lastBlockIndex: Long,
    val time: Long
)

data class AssetManageRes (
    val assetId: String,
    val name: String,
    val symbol: String,
    val balance: String = "0",
    val active: Boolean = false
)

data class OldConfigRes (
    val version_android: VersionAndroidRes?
)

data class VersionAndroidRes (
    val code: String,
    val tag: String,
    val url: String,
    val info: Map<String, String> = emptyMap()
)
