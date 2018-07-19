package com.iwallic.app.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.iwallic.app.R
import com.iwallic.app.models.PageDataRes
import com.iwallic.app.models.TransactionRes
import com.iwallic.app.utils.ConfigUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class TransactionAdapter(_data: PageDataRes<TransactionRes>): RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {
    private var data = _data
    private val _onEnter = PublishSubject.create<Int>()
    private val _onCopy = PublishSubject.create<Int>()
    private val VIEW_TYPE_CELL = 1
    private val VIEW_TYPE_FOOTER = 0
    private lateinit var pagerTV: TextView
    private var paging: Boolean = false
    // private var swiping = ListSwipingModel<FrameLayout, FrameLayout>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: FrameLayout
        if (viewType == VIEW_TYPE_CELL) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_transaction_list, parent, false) as FrameLayout
        } else {
            view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_pager, parent, false) as FrameLayout
            pagerTV = view.findViewById(R.id.adapter_pager)
            pagerTV.setText(if (data.data.size < data.total) R.string.list_loadmore else R.string.list_nomore)
        }
        return ViewHolder(view)
    }

    @Suppress("DEPRECATION")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == data.data.size) {
            return
        }
        val txidTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_txid)
        txidTV.text = data.data[position].txid
        val valueTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_value)
        valueTV.text = data.data[position].value
        val nameTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_name)
        nameTV.text = data.data[position].name
//        val bodyFL = holder.itemView.findViewById<FrameLayout>(R.id.adapter_transaction_list_body)
//        val copyFL = holder.itemView.findViewById<FrameLayout>(R.id.adapter_transaction_list_copy)
        if (data.data[position].value.startsWith("-")) {
            val color = ConfigUtils.attrColor(holder.itemView.context, R.attr.colorFont)
            txidTV.setTextColor(color)
            nameTV.setTextColor(color)
            valueTV.setTextColor(color)
            holder.itemView.findViewById<ImageView>(R.id.transaction_list_icon).setImageResource(R.drawable.icon_tx_out)
        }
        holder.itemView.setOnClickListener {
            _onEnter.onNext(position)
        }
//        holder.itemView.setOnLongClickListener {
//            _onCopy.onNext(position)
//            return@setOnLongClickListener true
//        }
//        holder.itemView.setOnTouchListener { view, motionEvent ->
//            val need = copyFL.width
//            when (motionEvent.action) {
//                MotionEvent.ACTION_UP -> {
//                    if (bodyFL.scrollX in 0..need/3) {
//                        bodyFL.scrollX = 0
//                        copyFL.scrollX = -need
//                    } else if (bodyFL.scrollX in need/3+1..need) {
//                        bodyFL.scrollX = need
//                        copyFL.scrollX = 0
//                    }
//                    Log.i("【swipe】", "release")
//                }
//                MotionEvent.ACTION_DOWN -> {
//                    if (swiping.view1 != null && swiping.position != position) {
//                        swiping.view1!!.scrollX = 0
//                        swiping.view2!!.scrollX = -need
//                        Log.i("【swipe】", "new tapped")
//                    }
//                    swiping.view1 = bodyFL
//                    swiping.view2 = copyFL
//                    swiping.lastX = motionEvent.x
//                    swiping.position = position
//                    Log.i("【swipe】", "tapped")
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    val move = (swiping.lastX - motionEvent.x) / 10
//                    bodyFL.scrollX = when {
//                        bodyFL.scrollX + move > need -> {
//                            need
//                        }
//                        bodyFL.scrollX + move < 0 -> {
//                            0
//                        }
//                        else -> {
//                            (bodyFL.scrollX + move).toInt()
//                        }
//                    }
//                    copyFL.scrollX = when {
//                        bodyFL.scrollX + move > need -> {
//                            0
//                        }
//                        bodyFL.scrollX + move < 0 -> {
//                            -need
//                        }
//                        else -> {
//                            (copyFL.scrollX + move).toInt()
//                        }
//                    }
//                }
//                MotionEvent.ACTION_CANCEL -> {
//                    if (bodyFL.scrollX in 0..need/3) {
//                        bodyFL.scrollX = 0
//                        copyFL.scrollX = -need
//                    } else if (bodyFL.scrollX in need/3+1..need) {
//                        bodyFL.scrollX = need
//                        copyFL.scrollX = 0
//                    }
//                }
//            }
//            true
//        }
    }

    override fun getItemCount() = data.data.size + 1

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == data.data.size) VIEW_TYPE_FOOTER else VIEW_TYPE_CELL
    }

//    fun onCopy(): Observable<Int> {
//        return _onCopy
//    }

    fun onEnter(): Observable<Int> {
        return _onEnter
    }

    fun push(newData: PageDataRes<TransactionRes>) {
        if (newData.page == 1) {
            data = newData
            notifyDataSetChanged()
        } else {
            val p = data.data.size
            data.page = newData.page
            // data.pageSize = newData.pageSize
            data.total = newData.total
            data.pageSize = newData.pageSize
            data.data.addAll(newData.data)
            notifyItemRangeInserted(p + 1, newData.data.size)
        }
        pagerTV.setText(if (data.data.size != data.total) R.string.list_loadmore else R.string.list_nomore)
        paging = false
    }

    fun checkNext(position: Int): Boolean {
        if (paging) {
            return false
        }
        return position == data.data.size && data.data.size != data.total
    }

    fun getPage(): Int {
        return data.page
    }

    fun setPaging() {
        if (paging) {
            return
        }
        paging = true
        pagerTV.setText(R.string.list_loading)
    }

    fun getItem(position: Int): TransactionRes {
        return data.data[position]
    }

    class ViewHolder(
        listView: FrameLayout
    ): RecyclerView.ViewHolder(listView)
}
