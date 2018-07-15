package com.iwallic.app.models

data class addrassets (
    val assetId: String,
    val balance: String,
    val name: String,
    val symbol: String
)

data class PageData<T>(
    var page: Int = 1,
    var pageSize: Int = 10,
    var total: Int = 0,
    var data: ArrayList<T> = arrayListOf()
)

data class transactions (
    val name: String,
    val txid: String,
    val value: String
)

data class blocktime (
    val lastBlockIndex: Long,
    val time: Long
)

data class assetmanage (
    val assetId: String,
    val name: String,
    val symbol: String,
    val balance: String = "0",
    val active: Boolean = false
)

data class OldConfig (
    val version_android: VersionAndroid?
)

data class VersionAndroid (
    val code: String,
    val tag: String,
    val url: String,
    val info: Map<String, String> = emptyMap()
)
