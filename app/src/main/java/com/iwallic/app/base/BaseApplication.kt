package com.iwallic.app.base

import android.app.Application
import android.content.Intent
import android.util.Log
import com.iwallic.app.services.BlockService
import com.iwallic.app.utils.CommonUtils

class BaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        CommonUtils.onConfigured().subscribe({
            if (it) {
                startService(Intent(this, BlockService::class.java))
            }
        }, {
            Log.i("【BaseActivity】", "config failed, block service will not on")
        })
    }
}
