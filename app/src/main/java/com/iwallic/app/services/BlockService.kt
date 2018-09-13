package com.iwallic.app.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.iwallic.app.broadcasts.BlockBroadCast
import com.iwallic.app.states.BlockState
import com.iwallic.app.states.UnconfirmedState
import com.iwallic.app.utils.CommonUtils
import com.iwallic.app.utils.SharedPrefUtils
import io.reactivex.disposables.Disposable
import java.util.*
import kotlin.concurrent.schedule

class BlockService : Service() {
    private var timer: Timer? = null
    private lateinit var blockListen: Disposable
    private lateinit var errorListen: Disposable

    override fun onCreate() {
        super.onCreate()
        Log.i("【BlockService】", "create")
        timer = Timer()
        timer!!.schedule(CommonUtils.listenPeried, CommonUtils.listenPeried) {
            BlockState.fetch(baseContext)
        }
        blockListen = BlockState.data().subscribe({
            BlockBroadCast.send(this, it.lastBlockIndex)
        }, {
            Log.i("【BlockService】", "error【$it】")
        })
        errorListen = BlockState.error().subscribe({
            Log.i("【BlockService】", "error【$it】")
        }, {
            Log.i("【BlockService】", "error【$it】")
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        timer!!.cancel()
        timer!!.purge()
        timer = null
        blockListen.dispose()
        Log.i("【BlockService】", "destroy")
    }

    override fun onBind(intent: Intent): IBinder {
        return BlockBinder
    }

    companion object BlockBinder: Binder()
}
