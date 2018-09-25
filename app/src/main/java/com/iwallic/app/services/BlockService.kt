package com.iwallic.app.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.iwallic.app.broadcasts.BlockBroadCast
import com.iwallic.app.states.BlockState
import com.iwallic.app.states.UnconfirmedState
import com.iwallic.app.utils.CommonUtils
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.SharedPrefUtils
import io.reactivex.disposables.Disposable
import java.util.*
import kotlin.concurrent.schedule

class BlockService : Service() {
    private var timer: TimerTask? = null

    override fun onCreate() {
        super.onCreate()
        Log.i("【BlockService】", "create")
        timer = Timer().schedule(CommonUtils.listenPeried, CommonUtils.listenPeried) {
            BlockState.fetch(applicationContext, { data, isNew ->
                if (isNew) {
                    Log.i("【BlockService】", "new block【${data.lastBlockIndex}】")
                    BlockBroadCast.send(applicationContext, data.lastBlockIndex, data.time)
                } else {
                    Log.i("【BlockService】", "no new block")
                }
            }, {
                Log.i("【BlockService】", "error【$it】")
                if (!DialogUtils.error(applicationContext, it)) {
                    Toast.makeText(applicationContext, "$it", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        Log.i("【BlockService】", "destroy")
    }

    override fun onBind(intent: Intent): IBinder {
        return BlockBinder
    }

    companion object BlockBinder: Binder()
}
