package com.iwallic.app.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.models.transactions

class TransactionAdapter(list: ArrayList<transactions>): RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {
    private var data = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_transaction_list, parent, false) as FrameLayout

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val txidTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_txid)
        txidTV.text = data[position].txid
        val valueTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_value)
        valueTV.text = data[position].value
        val nameTV = holder.itemView.findViewById<TextView>(R.id.transaction_list_name)
        nameTV.text = data[position].name
        if (data[position].value.startsWith("-")) {
            txidTV.setTextColor(R.attr.colorDefault)
            nameTV.setTextColor(R.attr.colorDefault)
            valueTV.setTextColor(R.attr.colorDefault)
            holder.itemView.findViewById<ImageView>(R.id.transaction_list_icon).setImageResource(R.drawable.icon_tx_out)
        }
        holder.itemView.setOnLongClickListener {
            val clipboard = it.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("TXID", data[position].txid)
            clipboard.primaryClip = clip
            Toast.makeText(it.context, R.string.error_copied, Toast.LENGTH_SHORT).show()

            val vibratorService = it.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibratorService.hasVibrator()) { // Vibrator availability checking
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibratorService.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)) // New vibrate method for API Level 26 or higher
                } else {
                    vibratorService.vibrate(100) // Vibrate method for below API Level 26
                }
            }

            return@setOnLongClickListener true
        }
    }

    override fun getItemCount() = data.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolder(
            listView: FrameLayout
    ): RecyclerView.ViewHolder(listView)
}
