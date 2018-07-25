package com.iwallic.app.utils

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.IBinder
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
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
    var net: String = "main"
    var pyApi: String = "http://192.168.1.106:5000" // http://192.168.1.106:5000 http://101.132.97.9:45005
    var mainApi: String = "https://api.iwallic.com/api/iwallic"
    var testApi: String = "https://teapi.iwallic.com/api/iwallic"
    var versionName: String = BuildConfig.VERSION_NAME
    private var configured = PublishSubject.create<Boolean>()
    private var lock: Boolean = false // need verify each time open wallet
    private var _setted = false
    private val _color = TypedValue()
    private val configQueue = arrayListOf<String>()
    fun apiGo(): String = if (net == "test") testApi else mainApi
    fun getAttrColor(context: Context, attr: Int): Int {
        context.theme.resolveAttribute(attr, _color, true)
        return _color.data
    }
    fun setNet(_net: String?, _pyApi: String? = null, _mainApi: String? = null, _testApi: String? = null) {
        net = _net ?: net
        pyApi = _pyApi ?: pyApi
        mainApi = _mainApi ?: mainApi
        testApi = _testApi ?: testApi
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
        if (configQueue.size >= 2) {
            _setted = true
            configured.onNext(true)
            configured.onComplete()
        }
    }
    fun notifyNetwork() {
        configQueue.add("network")
        if (configQueue.size >= 2) {
            _setted = true
            configured.onNext(true)
            configured.onComplete()
        }
    }
}
