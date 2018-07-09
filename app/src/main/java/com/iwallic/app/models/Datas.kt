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
        val data: ArrayList<T>? = arrayListOf()
)

data class transactions (
        val name: String,
        val txid: String,
        val value: Number
)
