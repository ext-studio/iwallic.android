package com.iwallic.app.utils

import android.content.Context
import android.util.TypedValue
import com.iwallic.app.BuildConfig
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object CommonUtils {
    const val NEO = "0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b"
    const val GAS = "0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7"
    const val EXT = "e8f98440ad0d7a6e76d84fb1c3d3f8a16e162e97"
    const val EDS = "81c089ab996fc89c468a26c0a88d23ae2f34b5c0"
    const val ACTION_NEWBLOCK = "com.iwallic.app.block"
    const val CHANNEL_DOWNLOAD = "com.iwallic.app.download"
    const val ID_DOWNLOAD = 1
    const val pyApi: String = "https://iwallic.forchain.info"
    const val mainApi: String = "http://101.132.97.9:8001/api/iwallic"
    const val testApi: String = "http://101.132.97.9:8002/api/iwallic"

    const val channelIDProgress = "progress_update"
    const val channelNameProgress = "更新进度"
    const val channelIDCommon = "common"
    const val channelNameCommon = "通用通知"

    const val notificationProgress = 985
    const val notificationCommon = 984

    var versionName: String = BuildConfig.VERSION_NAME
    private var configured = PublishSubject.create<Boolean>()
    private var lock: Boolean = false // need verify each time open wallet
    private var _setted = false
    private val _color = TypedValue()
    private val configQueue = arrayListOf<String>()
    fun getAttrColor(context: Context, attr: Int): Int {
        context.theme.resolveAttribute(attr, _color, true)
        return _color.data
    }
    /**
     * if not configured yet -> waiting
     * already configured -> emit true
     */
    fun onConfigured(): Observable<Boolean> {
        return if (_setted) {
            Observable.just(true)
        } else {
            configured
        }
    }
    fun notifyVersion() {
        configQueue.add("version")
        if (configQueue.size >= 1) {
            _setted = true
            configured.onNext(true)
            configured.onComplete()
        }
    }
}
