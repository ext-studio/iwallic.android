package com.iwallic.app.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.iwallic.app.states.BlockState
import io.reactivex.functions.Consumer
import java.util.*
import kotlin.concurrent.schedule

const val new_block_action = "com.iwallic.app.block"

class BlockService : Service() {
    private val newBlock = Intent(new_block_action)
    private var timer: Timer? = null

    override fun onCreate() {
        super.onCreate()
        Log.i("区块服务", "服务创建")
        timer = Timer()
        timer!!.schedule(10000, 20000) {
            Log.i("区块服务", "周期获取")
            BlockState.fetch()
        }
        BlockState.data().subscribe(Consumer {
            Log.i("区块服务", "新区块【"+it.lastBlockIndex+"】已到达")
            sendBroadcast(newBlock)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        timer!!.cancel()
        timer!!.purge()
        timer = null
        Log.i("区块服务", "服务销毁")
    }

    override fun onBind(intent: Intent): IBinder {
        return BlockBinder
    }

    companion object BlockBinder: Binder()
}
