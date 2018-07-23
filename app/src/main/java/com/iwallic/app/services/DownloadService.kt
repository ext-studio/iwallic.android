package com.iwallic.app.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.iwallic.app.R
import kotlinx.coroutines.experimental.launch
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.URL
import java.util.*
import kotlin.concurrent.schedule

class DownloadService : Service() {
    private val downloadProgressId = 1
    private val downloadChannelId = "com.iwallic.app.download"
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var downloading: Boolean = false
    private var percent = 0

    @Suppress("DEPRECATION")
    override fun onCreate() {
        super.onCreate()
        notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(applicationContext, downloadChannelId)
        } else {
            NotificationCompat.Builder(applicationContext)
        }
        val intent = Intent(this, DownloadService::class.java)
        intent.putExtra("action", "cancel")
        val cancelIntent = PendingIntent.getService(this, 0, intent, 0)
        notificationBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.icon_delete, "取消下载", cancelIntent).build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra("action") ?: "") {
            "" -> {
                Log.i("【DownloadService】", "start")
                val url = intent?.getStringExtra("url") ?: ""
                if (!downloading && url.isNotEmpty()) {
                    resolveDownload(url)
                }
            }
            "install" -> {
                // todo if downloaded open apk file else do nothing
            }
            "cancel" -> {
                resolveCancel()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        return DownloadBinder
    }

    private fun resolveDownload(_url: String) {
        downloading = true
        setProgressNotification(percent)
        launch {
            try {
                val url = URL(_url)
                val connection = url.openConnection()
                connection.connect()
                val fileLength = connection.contentLength

                val input = BufferedInputStream(connection.getInputStream())
                val output = FileOutputStream("${filesDir.absolutePath}/installer.${System.currentTimeMillis()/1000}.apk")

                val data = ByteArray(fileLength)
                var total: Long = 0
                var count = 0
                while (count != -1) {
                    count = input.read(data)
                    total += count.toLong()
                    output.write(data, 0, count)
                    percent = (total * 100 / fileLength).toInt()
                }
                output.flush()
                output.close()
                input.close()

                Timer().schedule(2000, 2000){
                    if (percent >= 100) {
                        cancel()
                        setProgressNotification(percent, true)
                        resolveInstall()
                    } else {
                        setProgressNotification(percent)
                    }
                }
            } catch (e: Throwable) {
                Log.i("【DownloadService】", "$e")
                stopSelf()
            }
        }
    }

    private fun resolveCancel() {
        // todo delete file & cancel download
        downloading = false
        stopSelf()
    }

    private fun resolveInstall() {
        Log.i("【DownloadService】", "install")
    }

    private fun setProgressNotification(percent: Int = 0, indeterminate: Boolean = false) {
        notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(resources,R.mipmap.ic_launcher))
                .setContentTitle("Getting package")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setProgress(100, percent, indeterminate)
                .setContentText("$percent%")
                .setWhen(System.currentTimeMillis())
        val notification = notificationBuilder.build()
        startForeground(downloadProgressId, notification)
    }

    companion object DownloadBinder: Binder()
}
