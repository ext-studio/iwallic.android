package com.iwallic.app.utils

import android.content.Context
import android.util.TypedValue
import com.iwallic.app.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object ConfigUtils {
    var setted = PublishSubject.create<Boolean>()
    var net: String = "main"
    var mainApi: String = "https://api.iwallic.com/api/iwallic"
    var testApi: String = "https://teapi.iwallic.com/api/iwallic"
    var lock: Boolean = false // need verify each time open wallet
    private var _setted = false
    private val _color = TypedValue()
    fun api(): String = if (net == "test") testApi else mainApi
    fun set(_net: String?, _mainApi: String? = null, _testApi: String? = null) {
        net = _net ?: net
        mainApi = _mainApi ?: mainApi
        testApi = _testApi ?: testApi
        _setted = true
        setted.onNext(true)
        setted.onComplete()
    }
    fun listen(): Observable<Boolean> {
        return if (_setted) {
            Observable.just(true)
        } else {
            setted
        }
    }
    fun attrColor(context: Context, attr: Int): Int {
        context.theme.resolveAttribute(attr, _color, true)
        return _color.data
    }
}
