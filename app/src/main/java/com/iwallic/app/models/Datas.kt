package com.iwallic.app.models

data class BalanceRes (
    val assetId: String,
    val balance: String,
    val name: String,
    val symbol: String
)

data class PageDataRes<T>(
    var page: Int = 1,
    var pageSize: Int = 30,
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

data class AssetManageRes (
    val assetId: String = "",
    val name: String = "",
    val symbol: String = "",
    val balance: String = "0",
    var display: Boolean = false
)

data class OldConfigRes (
    val version_android: VersionAndroidRes?
)

data class VersionAndroidRes (
    val code: String = "1.0.0",
    val tag: String = "Beta",
    val url: String = "",
    val info: Map<String, String> = emptyMap()
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

data class ListSwipingModel<T1, T2> (
    var view1: T1? = null,
    var view2: T2? = null,
    var position: Int = -1,
    var lastX: Float = -1f,
    var opening: Boolean = true,
    var base: Float = 0f
)
