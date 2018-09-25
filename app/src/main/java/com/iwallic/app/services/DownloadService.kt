package com.iwallic.app.services

import android.app.*
import android.content.Intent
import android.content.Context
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.content.FileProvider
import android.util.Log
import com.iwallic.app.R
import com.iwallic.app.utils.NotificationUtils
import kotlinx.coroutines.experimental.launch
import java.io.*
import java.net.URL
import java.util.*
import kotlin.concurrent.schedule

class DownloadService : Service() {
    private var percent = 0
    private var timerTask: TimerTask? = null
    private var canceled = false
    private var started = false

    override fun onCreate() {
        super.onCreate()
        Log.i("【DownloadService】", "created")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("【DownloadService】", "destroyed")
    }
    override fun onBind(intent: Intent?): IBinder {
        return DownloadBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getStringExtra("action") ?: ""
        when (action) {
            "cancel" -> {
                canceled = true
            }
            "download" -> {
                val url = intent?.getStringExtra("url") ?: ""
                if (url.isEmpty() || started) {
                    stopSelf()
                } else {
                    started = true
                    resolve(url)
                }
            }
            else -> {
//                stopSelf()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun resolve(url: String) {
        val service = this
        launch {
            try {
                canceled = false
                NotificationUtils.progress(service, "", 0)
                timerTask = Timer().schedule(1000, 1000) {
                    NotificationUtils.progress(service, R.string.version_updating, percent)
                }
                val file = resolveDownload(url)
                if (file == null) {
                    NotificationUtils.common(applicationContext, R.string.version_error, R.string.version_error_download)
                } else {
                    NotificationUtils.progress(service, R.string.version_finishing, 100)
                    resolveInstall(file)
                }
            } catch (e: Throwable) {
                Log.i("【DownloadService】", "error【$e】")
                NotificationUtils.common(applicationContext, R.string.version_error, R.string.version_error_unknown)
                stopSelf()
            }
            timerTask?.cancel()
            stopSelf()
        }
    }

    private fun resolveDownload(_url: String): File? {
        val url = URL(_url)
        val parent = File(cacheDir, "installers")
        if (!parent.exists()) {
            parent.mkdir()
            try {
                val command = "chmod 777 " + parent.absolutePath
                val runtime = Runtime.getRuntime()
                runtime.exec(command)
            } catch (e: IOException) {
                NotificationUtils.common(applicationContext, R.string.version_error, R.string.version_error_permission)
            }
        }
        val file = File(parent, "TheToken_${System.currentTimeMillis()}.apk")
        val connection = url.openConnection()
        connection.connect()
        connection.readTimeout = 5000
        connection.connectTimeout = 5000

        val fileLength = connection!!.contentLength
        val input = BufferedInputStream(url.openStream(), 8192)
        Log.i("【DownloadService】", "start download")
        val output = FileOutputStream(file)
        val data = ByteArray(1024)
        var total: Long = 0
        var count = input.read(data)
        while (count != -1) {
            if (canceled) {
                Log.i("【DownloadService】", "canceled")
                output.flush()
                output.close()
                input.close()
                return null
            }
            total += count
            output.write(data, 0, count)
            percent = (total * 100 / fileLength).toInt()
            count = input.read(data)
            if (count == -1) {
                break
            }
        }
        output.flush()
        output.close()
        input.close()
        return file
    }

    private fun resolveInstall(file: File) {
        Log.i("【DownloadService】", "start install")
        val installIntent = Intent(Intent.ACTION_VIEW)
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        installIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val apkUri = FileProvider.getUriForFile(applicationContext, applicationContext.packageName + ".provider", file)
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            startActivity(installIntent)
        } else {
            val apkUri = Uri.fromFile(file) // Uri.parse("file://${file}") // Uri.fromFile(file)
            try {
                val command = "chmod 777 " + file.absolutePath
                val runtime = Runtime.getRuntime()
                runtime.exec(command)
                installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                startActivity(installIntent)
            } catch (e: IOException) {
                NotificationUtils.common(applicationContext, R.string.version_error, R.string.version_error_permission)
            }
        }
    }

    companion object DownloadBinder: Binder() {
        @JvmStatic
        fun start(context: Context, url: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                putExtra("action", "download")
                putExtra("url", url)
            }
            context.startService(intent)
        }
    }
}
