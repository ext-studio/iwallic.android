package com.iwallic.app.base

import android.app.Application
import android.content.Intent
import android.support.multidex.MultiDexApplication
import com.iwallic.app.pages.common.WelcomeActivity
import com.iwallic.app.utils.CommonUtils
import com.tencent.smtt.sdk.QbSdk

class BaseApplication: MultiDexApplication(), Thread.UncaughtExceptionHandler {
    override fun onCreate() {
        super.onCreate()
        CommonUtils.initNeonApi(this)
        QbSdk.initX5Environment(this, null)
    }
    override fun uncaughtException(p0: Thread?, p1: Throwable?) {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
