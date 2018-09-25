package com.iwallic.app.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.iwallic.app.utils.CommonUtils

class AssetBroadCast: BroadcastReceiver() {
    private var assetChangedListener: ((Context?, Intent?) -> Unit)? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        assetChangedListener?.invoke(context, intent)
    }

    fun setOnAssetChangedListener(listener: (Context?, Intent?) -> Unit) {
        assetChangedListener = listener
    }

    companion object {
        fun send(context: Context) {
            val intent = Intent(CommonUtils.broadCastAsset)
            context.sendBroadcast(intent)
        }
    }
}
