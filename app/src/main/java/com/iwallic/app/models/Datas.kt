package com.iwallic.app.models

data class addrassets (
    val assetId: String,
    val balance: String,
    val name: String,
    val symbol: String
)

data class pageData<T>(
    val page: Int,
    val pageSize: Int,
    val total: Int,
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