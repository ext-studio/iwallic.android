package com.iwallic.app.pages.main

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.base.BaseFragment
import com.iwallic.app.broadcasts.BlockBroadCast
import com.iwallic.app.states.BlockState
import com.iwallic.app.utils.CommonUtils
import com.iwallic.app.utils.DialogUtils
import java.text.SimpleDateFormat
import java.util.*

class FindFragment : BaseFragment() {
    private lateinit var heightTV: TextView
    private lateinit var timeTV: TextView
    private var broadCast: BlockBroadCast? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_find, container, false)
        initDOM(view)
        initBlock()
        initBroadCast()
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(broadCast)
    }

    private fun initDOM(view: View) {
        heightTV = view.findViewById(R.id.find_height)
        timeTV = view.findViewById(R.id.find_time)
    }
    @SuppressLint("SimpleDateFormat")
    private fun initBlock() {
        BlockState.current(context, { data, _ ->
            heightTV.text = resources.getString(R.string.find_height, data.lastBlockIndex)
            timeTV.text = resources.getString(R.string.find_time, SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(data.time*1000)))
        }, {
            if (!DialogUtils.error(context, it)) {
                Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
            }
        })
    }
    @SuppressLint("SimpleDateFormat")
    private fun initBroadCast() {
        broadCast = BlockBroadCast()
        broadCast?.setNewBlockListener { _, intent ->
            val height = intent?.getLongExtra("height", 0) ?: 0
            val time = intent?.getLongExtra("time", 0) ?: 0
            heightTV.text = resources.getString(R.string.find_height, if (height > 0) height else null)
            timeTV.text = resources.getString(R.string.find_time, if (time > 0) SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(time*1000)) else null)
        }
        context?.registerReceiver(broadCast, IntentFilter(CommonUtils.broadCastBlock))
    }
}
