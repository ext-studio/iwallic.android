package com.iwallic.app.utils

import android.content.Context
import android.util.TypedValue
import com.iwallic.app.BuildConfig
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object CommonUtils {
    var net: String = "main"
    var pyApi: String = "http://101.132.97.9:45005"
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
