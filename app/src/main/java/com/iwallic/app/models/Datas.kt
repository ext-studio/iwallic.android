package com.iwallic.app.models

data class BalanceRes (
    val assetId: String,
    val balance: String,
    val name: String,
    val symbol: String
)

data class PageDataRes<T>(
    var page: Int = 1,
    var pageSize: Int = 20,
    var total: Int = 0,
    var data: ArrayList<T> = arrayListOf()
)

data class TransactionRes (
    val name: String,
    val txid: String,
    val value: String
)

data class TransactionDetailRes (
    val from: String,
    val to: String,
    var address: String,
    val name: String,
    val value: Double,
    val blockIndex: Int,
    val time: Double
)

data class TransactionDetailFromRes<T>(
    val TxUTXO: ArrayList<TransactionDetailRes>
)

data class TransactionDetailToRes<T>(
    val TxVouts: ArrayList<TransactionDetailRes>
)

data class BlockTimeRes (
    val lastBlockIndex: Long,
    val time: Long
)

data class AssetManageRes (
    val assetId: String = "",
    val name: String = "",
    val symbol: String = "",
    val balance: String = "0",
    var display: Boolean = false
)

data class VersionRes (
    val code: Int = 10,
    val name: String = "1.0.0",
    val tag: String = "Beta",
    val url: String = "",
    val info: Map<String, String> = emptyMap()
)

data class ResponsePyModel (
    val msg: String = "",
    val data: Any?,
    val error_code: Int?,
    val bool_status: Boolean = false
)

data class PageDataPyModel<T> (
    var items: ArrayList<T> = arrayListOf(),
    var page: Int = 1,
    var pages: Int = 1,
    var per_page: Int = 20,
    var total: Int = 0
)

data class AssetListPyModel (
    val assetId: String = "",
    val name: String = "",
    val symbol: String = ""
)

data class RequestGoModel (
        val method: String,
        val params: List<Any>
)

data class ResponseGoModel (
        val code: Int,
        val msg: String,
        val result: Any?
)
