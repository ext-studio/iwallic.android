package com.iwallic.app.models

data class ScryptModel(
    val n: Int = 16384,
    val r: Int = 8,
    val p: Int = 8
)

data class ContractParameterModel(
    val name: String = "signature",
    val type: String = "Signature"
)

data class ContractModel(
    val script: String,
    var parameters: ArrayList<ContractParameterModel>? = arrayListOf(ContractParameterModel()),
    val deployed: Boolean = false
)

data class AccountModel(
    val address: String,
    var label: ArrayList<String> = arrayListOf(),
    val isDefault: Boolean = false,
    val lock: Boolean = false,
    val key: String,
    val contract: ContractModel,
    val extra: String = ""
)

data class WalletModel(
    val name: String = "iWallic",
    val version: String = "1.0",
    val scrypt: ScryptModel = ScryptModel(),
    var accounts: ArrayList<AccountModel>,
    val extra: String = ""
)

// For management, saved in sql
data class WalletAgentModel(
    val _ID: Long,
    val file: String,
    val snapshot: String,
    val count: Int,
    val updateAt: Long
)
