package com.iwallic.app.utils

import android.content.Context
import android.util.TypedValue
import com.iwallic.app.BuildConfig
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

object CommonUtils {
    const val NEO = "0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b"
    const val GAS = "0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7"
    const val EXT = "e8f98440ad0d7a6e76d84fb1c3d3f8a16e162e97"
    const val EDS = "81c089ab996fc89c468a26c0a88d23ae2f34b5c0"
    const val ACTION_NEWBLOCK = "com.iwallic.app.block"
    const val CHANNEL_DOWNLOAD = "com.iwallic.app.download"
    const val ID_DOWNLOAD = 1
    const val pyApi: String = "https://iwallic.forchain.info"
    const val mainApi: String = "https://api.iwallic.forchain.info/api/iwallic"
//    const val pyApi: String = "http://101.132.97.9:45005/"
//    const val mainApi: String = "http://101.132.97.9:8001/api/iwallic"
    const val testApi: String = "http://101.132.97.9:8002/api/iwallic"

    const val channelIDProgress = "progress_update"
    const val channelNameProgress = "更新进度"
    const val channelIDCommon = "common"
    const val channelNameCommon = "通用通知"

    const val notificationProgress = 985
    const val notificationCommon = 984
    const val requestBalanceUpdated = 983

    const val listenPeried: Long = 60000

    const val pageCount = 15

    const val broadCastBlock = "iwallic_new_block"
    const val broadCastTx = "iwallic_new_tx"
    const val broadCastAsset = "iwallic_asset"

    private val _color = TypedValue()

    fun getAttrColor(context: Context, attr: Int): Int {
        context.theme.resolveAttribute(attr, _color, true)
        return _color.data
    }

    fun getStatusBarHeight(context: Context): Double {
        return Math.ceil(25 * context.resources.displayMetrics.density.toDouble())
    }

    fun <T>safeSlice(list: ArrayList<T>, start: Int, take: Int): ArrayList<T> {
        if (list.size == 0 || start >= list.size) {
            return arrayListOf()
        }
        return if (list.size - start - 1 < take) {
            ArrayList(list.subList(start, list.size))
        } else {
            ArrayList(list.subList(start, start + take))
        }
    }
}
