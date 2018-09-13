package com.iwallic.app.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.res.ResourcesCompat
import com.iwallic.app.R
import com.iwallic.app.services.DownloadService

object NotificationUtils {
    private var progressBuilder: NotificationCompat.Builder? = null
    private var commonBuilder: NotificationCompat.Builder? = null
    fun progress(service: Service, title: String, progress: Int) {
        if (progressBuilder == null) {
            progressBuilder = resolveProgressBuilder(service)
        }
        progressBuilder?.setWhen(System.currentTimeMillis())
        progressBuilder?.setContentTitle(title)
        progressBuilder?.setContentText("$progress%")
        progressBuilder?.setProgress(100, progress, progress == 0 || progress == 100)
        val notification = progressBuilder?.build()
        service.startForeground(CommonUtils.notificationProgress, notification)
    }
    fun common(context: Context, title: String, content: String) {
        if (commonBuilder == null) {
            commonBuilder = resolveCommonBuilder(context)
        }
        commonBuilder?.setWhen(System.currentTimeMillis())
        commonBuilder?.setContentTitle(title)
        if (content.isNotEmpty()) {
            commonBuilder?.setContentText(content)
        }
        val notification = commonBuilder?.build()
        NotificationManagerCompat.from(context).notify(CommonUtils.notificationCommon, notification!!)
    }
    private fun resolveProgressBuilder(context: Context): NotificationCompat.Builder {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, resolveChannel(context, CommonUtils.channelIDProgress, CommonUtils.channelNameProgress, "令牌(TheToken)更新进度提示"))
        } else {
            @Suppress("DEPRECATION")
            NotificationCompat.Builder(context)
        }
        val intent = Intent(context, DownloadService::class.java)
        intent.putExtra("action", "cancel")
        val cancelIntent = PendingIntent.getService(context, 0, intent, 0)
        builder.addAction(NotificationCompat.Action.Builder(R.drawable.icon_delete, "Cancel", cancelIntent).build())
        builder.setLargeIcon(BitmapFactory.decodeResource(context.resources,R.mipmap.ic_launcher))
        builder.setSmallIcon(R.mipmap.ic_launcher)
        return builder
    }
    private fun resolveCommonBuilder(context: Context): NotificationCompat.Builder {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, resolveChannel(context, CommonUtils.channelIDCommon, CommonUtils.channelNameCommon, "令牌(TheToken)一般性通知"))
        } else {
            @Suppress("DEPRECATION")
            NotificationCompat.Builder(context)
        }
        builder.setLargeIcon(BitmapFactory.decodeResource(context.resources,R.mipmap.ic_launcher))
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.build()
        builder.build()
        return builder
    }
    @SuppressLint("NewApi")
    private fun resolveChannel(context: Context, id: String, name: String, desc: String = ""): String {
        val service = context.getSystemService(NotificationManager::class.java)
        val chan = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
        if (desc.isNotEmpty()) {
            chan.description = desc
        }
        chan.lightColor = CommonUtils.getAttrColor(context, R.attr.colorPrimary)
        service.createNotificationChannel(chan)
        return id
    }
}
