package com.iwallic.app.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.FileProvider
import android.util.Log
import com.iwallic.app.R
import com.iwallic.app.utils.CommonUtils
import io.reactivex.Observable
import kotlinx.coroutines.experimental.launch
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*
import kotlin.concurrent.schedule

class DownloadService : Service() {
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var state = "waiting"
    private var percent = 0
    private var filePath = ""

    @Suppress("DEPRECATION")
    override fun onCreate() {
        super.onCreate()
        notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(applicationContext, CommonUtils.CHANNEL_DOWNLOAD)
        } else {
            NotificationCompat.Builder(applicationContext)
        }
        val intent = Intent(this, DownloadService::class.java)
        intent.putExtra("action", "cancel")
        val cancelIntent = PendingIntent.getService(this, 0, intent, 0)
        val openIntent = Intent(this, DownloadService::class.java)
        openIntent.putExtra("action", "install")
        val openPIntent = PendingIntent.getService(this, 0, openIntent, 0)
        notificationBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.icon_delete, "Cancel", cancelIntent).build())
        notificationBuilder.setContentIntent(openPIntent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra("action") ?: "") {
            "" -> {
                Log.i("【DownloadService】", "start")
                val url = intent?.getStringExtra("url") ?: ""
                if (state == "waiting" && url.isNotEmpty()) {
                    resolveDownload(url)
                    resolveListen().subscribe({
                        when {
                            it == 101 -> {
                                setProgressNotification()
                                resolveInstall()
                            }
                            it < 0 -> {
                                setProgressNotification()
                                resolveFailed()
                            }
                            else -> {
                                setProgressNotification()
                            }
                        }
                    }, {
                        Log.i("【DownloadService】", "error 【$it】")
                        stopSelf()
                    })
                }
            }
            "install" -> {
                resolveInstall()
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
            state = "downloading"
            launch {
                try {
                    filePath = ""
                    val url = URL(_url)
                    val connection = url.openConnection()
                    connection.connect()
                    val fileLength = connection.contentLength

                    val input = BufferedInputStream(url.openStream(), 8192)
                    val path = getFileName(_url)
                    val file = File(filesDir, path)
                    if (file.exists()) {
                        Log.i("【DownloadService】", "complete 【exists】")
                        filePath = path
                        percent = 101
                        state = "finished"
                        return@launch
                    }
                    Log.i("【DownloadService】", "start download")
                    val output = FileOutputStream(File(filesDir, path))

                    val data = ByteArray(1024)
                    var total: Long = 0
                    var count = input.read(data)
                    while (count != -1) {
                        total += count
                        output.write(data, 0, count)
                        percent = (total * 100 / fileLength).toInt()
                        count = input.read(data)
                        if (count == -1) {
                            break
                        }
                    }
                    filePath = path
                    percent = 101
                    state = "finished"
                    output.flush()
                    output.close()
                    input.close()
                    Log.i("【DownloadService】", "finish download")
                } catch (e: Throwable) {
                    Log.i("【DownloadService】", "error【$e】")
                    state = "failed"
                    percent = -1
                    // stopSelf()
                }
            }
    }

    private fun resolveFailed() {
//        state = "waiting"
        percent = 0
        val file = File(filesDir, filePath)
        if (file.exists()) {
            file.delete()
        }
    }
    private fun resolveCancel() {
        val file = File(filesDir, filePath)
        if (file.exists()) {
            file.delete()
        }
        stopSelf()
    }

    private fun resolveListen(): Observable<Int> {
        return when (state) {
            "finished" -> {
                Observable.just(101)
            }
            "installing" -> {
                Observable.just(101)
            }
            "failed" -> {
                Observable.just(-1)
            }
            else -> Observable.create {observer ->
                Timer().schedule(1000, 1000) {
                    observer.onNext(percent)
                    if (percent == 101) {
                        cancel()
                        observer.onComplete()
                    }
                }
            }
        }
    }
    private fun resolveInstall() {
        try {
            if (state == "installing") {
                return
            }
            state = "installing"
            Log.i("【DownloadService】", "install")
            val file = File(filesDir, filePath)
            var uri = Uri.fromFile(file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (Build.VERSION.SDK_INT >= 24) {
                uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            startActivity(intent)
            state = "finished"
        } catch (e: Throwable) {
            Log.i("【DownloadService】", "error【$e】")
            state = "failed"
            percent = -1
            // stopSelf()
        }
    }

    private fun setProgressNotification() {
        notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(resources,R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
        when (state) {
            "waiting" -> {
                notificationBuilder.setContentTitle("正在准备")
                        .setProgress(100, 0, true)
                        .setContentText("0%")
            }
            "downloading" -> {
                notificationBuilder.setContentTitle("正在下载")
                        .setProgress(100, percent, false)
                        .setContentText("$percent%")
            }
            "installing" -> {
                notificationBuilder.setContentTitle("正在安装")
                        .setProgress(100, 100, true)
                        .setContentText("100%")
            }
            "failed" -> {
                notificationBuilder.setContentTitle("操作失败")
                        .setProgress(100, 0, true)
                        .setContentText("")
            }
            else -> {
                notificationBuilder.setContentTitle("正在完成")
                        .setProgress(100, percent, true)
                        .setContentText("100%")
            }
        }
        val notification = notificationBuilder.build()
        startForeground(CommonUtils.ID_DOWNLOAD, notification)
    }

    private fun getFileName(src: String): String {
        return src.substring(src.indexOfLast{it == '/'})
    }

    companion object DownloadBinder: Binder()
}
