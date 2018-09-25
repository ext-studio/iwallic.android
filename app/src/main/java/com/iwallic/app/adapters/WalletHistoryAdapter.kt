package com.iwallic.app.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.models.WalletAgentModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class WalletHistoryAdapter(_list: ArrayList<WalletAgentModel>): RecyclerView.Adapter<WalletHistoryAdapter.ViewHolder>() {
    private val data = _list
    private var onChooseListener: ((Int) -> Unit)? = null
    private var onDeleteListener: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_wallet_history, parent, false) as LinearLayout
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.adapter_wallet_history_snapshot).text = data[position].snapshot
        holder.itemView.findViewById<TextView>(R.id.adapter_wallet_history_count).text = holder.itemView.context.resources.getString(R.string.wallet_history_count, data[position].count)
        holder.itemView.findViewById<FrameLayout>(R.id.adapter_wallet_history_enter).setOnClickListener {
            onChooseListener?.invoke(position)
        }
        holder.itemView.findViewById<ImageView>(R.id.adapter_wallet_history_del).setOnClickListener {
            onDeleteListener?.invoke(position)
        }
    }

    override fun getItemCount() = data.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getData(position: Int): WalletAgentModel {
        return data[position]
    }

    fun setOnChooseListener(listener: (Int) -> Unit) {
        onChooseListener = listener
    }

    fun setOnDeleteListener(listener: (Int) -> Unit) {
        onDeleteListener = listener
    }

    fun remove(position: Int) {
        data.removeAt(position)
        notifyItemRemoved(position)
    }

    class ViewHolder (
         listView: LinearLayout
    ): RecyclerView.ViewHolder(listView)
}
