package com.iwallic.app.base

import android.app.Application
import android.content.Intent
import com.iwallic.app.pages.common.WelcomeActivity
import com.iwallic.app.services.X5IntentService

class BaseApplication: Application(), Thread.UncaughtExceptionHandler {
    override fun onCreate() {
        super.onCreate()
        X5IntentService.init(this)
    }
    override fun uncaughtException(p0: Thread?, p1: Throwable?) {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
