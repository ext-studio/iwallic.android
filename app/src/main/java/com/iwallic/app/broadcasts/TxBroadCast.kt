package com.iwallic.app.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.iwallic.app.utils.CommonUtils

class TxBroadCast: BroadcastReceiver() {
    private var transferListener: ((Context?, Intent?) -> Unit)? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        transferListener?.invoke(context, intent)
    }

    fun setOnTransferredListener(listener: (Context?, Intent?) -> Unit) {
        transferListener = listener
    }

    companion object {
        fun send(context: Context, txid: String) {
            val intent = Intent(CommonUtils.broadCastTx)
            intent.putExtra("txid", txid)
            context.sendBroadcast(intent)
        }
    }
}