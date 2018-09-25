package com.iwallic.app.broadcasts

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.iwallic.app.utils.CommonUtils.broadCastBlock

class BlockBroadCast: BroadcastReceiver() {
    private var newBlockListener: ((Context?, Intent?) -> Unit)? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        newBlockListener?.invoke(context, intent)
    }

    fun setNewBlockListener(listener: (Context?, Intent?) -> Unit) {
        newBlockListener = listener
    }

    companion object {
        fun send(context: Context, height: Long, time: Long) {
            val intent = Intent(broadCastBlock)
            intent.putExtra("height", height)
            intent.putExtra("time", time)
            context.sendBroadcast(intent)
        }
    }
}
