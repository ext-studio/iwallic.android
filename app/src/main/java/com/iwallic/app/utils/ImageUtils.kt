package com.iwallic.app.utils

import android.graphics.Bitmap
import android.os.Environment
import android.util.LruCache
import android.widget.ImageView
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.io.FileOutputStream
import android.util.Log
import io.reactivex.Observable
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import java.net.URL

object ImageUtils {
    private val memCache = LruCache<String, Bitmap>((java.lang.Runtime.getRuntime().maxMemory()/8).toInt())
    private val filePath = Environment.getExternalStorageDirectory().absolutePath + "/cached_img"
    fun setUrl(view: ImageView, url: String) {
        launch {
            var bitmap = getFromMemCache(url)
            if (bitmap != null) {
                Log.i("【ImageUtils】", "from cache")
                withContext(UI) {
                    view.setImageBitmap(bitmap)
                }
                return@launch
            }
            bitmap = getFromFileCache(url)
            if (bitmap != null) {
                Log.i("【ImageUtils】", "from file")
                withContext(UI) {
                    view.setImageBitmap(bitmap)
                }
                return@launch
            }
            getFromNetwork(url).subscribe({
                Log.i("【ImageUtils】", "from net")
                view.setImageBitmap(it)
            }, {
                Log.i("【ImageUtils】", "failed")
            })
        }
    }
    private fun getFromMemCache(url: String): Bitmap? {
        return memCache.get(url)
    }
    private fun setToMemCache(url: String, content: Bitmap) {
        memCache.put(url, content)
    }
    private fun getFromFileCache(url: String): Bitmap? {
        try {
            val fileName = encodeMD5(url)
            val file = File(filePath, fileName)
            if (file.exists()) {
                return BitmapFactory.decodeStream(FileInputStream(file))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    private fun setToFileCache(url: String, content: Bitmap) {
        try {
            val fileName = encodeMD5(url)
            val file = File(filePath, fileName)
            val parentFile = file.parentFile
            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }
            content.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun getFromNetwork(url: String): Observable<Bitmap> {
        return Observable.create {
            launch {
                try {
                    val stream = URL(url).openStream()
                    val rs = BitmapFactory.decodeStream(stream)
                    if (rs != null) {
                        withContext(UI) {
                            it.onNext(rs)
                        }
                        it.onComplete()
                    } else {
                        it.onError(Throwable("99999"))
                    }
                } catch (e: Throwable) {
                    it.onError(e)
                }
            }
        }
    }
    private fun encodeMD5(src: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(src.toByteArray())
        val sb = StringBuffer()
        for (b in digest) {
            val i :Int = b.toInt() and 0xff
            var hexString = Integer.toHexString(i)
            if (hexString.length < 2) {
                hexString = "0$hexString"
            }
            sb.append(hexString)
        }
        return sb.toString()
    }
}