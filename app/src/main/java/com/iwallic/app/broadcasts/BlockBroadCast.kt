package com.iwallic.app.broadcasts

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.iwallic.app.utils.CommonUtils.broadCastBlock

class BlockBroadCast: BroadcastReceiver() {
    private var updateListener: ((Context?, Intent?) -> Unit)? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        updateListener?.invoke(context, intent)
    }

    fun setOnUpdatedListener(listener: (Context?, Intent?) -> Unit) {
        updateListener = listener
    }

    companion object {
        fun send(context: Context, height: Long) {
            val intent = Intent(broadCastBlock)
            intent.putExtra("height", height)
            context.sendBroadcast(intent)
        }
    }
}
