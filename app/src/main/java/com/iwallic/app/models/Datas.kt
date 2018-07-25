package com.iwallic.app.models

//data class BalanceRes (
//    val assetId: String = "",
//    val balance: String = "0",
//    val name: String = "unknown"
//)

data class PageDataRes<T>(
    var page: Int = 1,
    var pageSize: Int = 10,
    var total: Int = 0,
    var data: ArrayList<T> = arrayListOf()
)

data class TransactionRes (
    val name: String = "",
    val txid: String = "",
    val value: String = ""
)

data class TransactionDetailRes (
        val from: String = "",
        val to: String = "",
        var address: String = "",
        val name: String = "",
        val value: Double = 0.0,
        val blockIndex: Int = 0,
        val time: Double = 0.0
)

data class TransactionDetailFromRes(
    val TxUTXO: ArrayList<TransactionDetailRes> = arrayListOf()
)

data class TransactionDetailToRes(
    val TxVouts: ArrayList<TransactionDetailRes> = arrayListOf()
)

data class BlockTimeRes (
    val lastBlockIndex: Long,
    val time: Long
)

data class AssetRes (
    val asset_id: String = "",
    val balance: String = "0",
    val name: String = "unknown",
    val symbol: String = "unknown"
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
    var per_page: Int = 10,
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

data class ClaimsRes (
    val unSpentClaim: String = "0",
    val unCollectClaim: String = "0",
    var claims: ArrayList<ClaimItemRes> = arrayListOf()
)

data class ClaimItemRes (
    val txid: String = "",
    val claim: String = "",
    val index: String = ""
)
