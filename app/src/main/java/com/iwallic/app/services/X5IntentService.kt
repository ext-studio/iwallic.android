package com.iwallic.app.services

import android.app.IntentService
import android.content.Intent
import android.content.Context
import com.tencent.smtt.sdk.QbSdk

class X5IntentService : IntentService("X5IntentService") {
    override fun onHandleIntent(intent: Intent?) {
        QbSdk.initX5Environment(applicationContext, null)
    }

    companion object {
        @JvmStatic
        fun init(context: Context) {
            val intent = Intent(context, X5IntentService::class.java)
            context.startService(intent)
        }
    }
}
