package com.iwallic.app.services

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.iwallic.app.R
import com.iwallic.app.models.OldConfig
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.HttpClient

class UpdateIntentService : IntentService("UpdateIntentService") {
    val gson = Gson()
    override fun onHandleIntent(intent: Intent?) {
        HttpClient.post("fetchIwallicConfig", ok = fun (res) {
            val config = gson.fromJson<OldConfig>(res, OldConfig::class.java)
            if (config?.version_android != null) {
                if (config.version_android.code != "1.0.0") {
                    Log.i("版本检查", "发现新版本")
//                    DialogUtils.Dialog(
//                        this,
//                        R.string.dialog_title_primary,
//                        R.string.dialog_version_new_body,
//                        R.string.dialog_version_ok,
//                        R.string.dialog_no,
//                        fun (confirm) {
//                            if (confirm) {
//                                val uri = Uri.parse(config.version_android.url)
//                                startActivity(Intent(Intent.ACTION_VIEW, uri))
//                            }
//                        }
//                    )
                    return
                }
            }
            Log.i("版本检查", "已经是最新版本")
        }, no = fun (err) {
            if (!DialogUtils.Error(baseContext, err)) {
                Toast.makeText(baseContext, "$err", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        @JvmStatic
        fun versionCheck(context: Context) {
            val intent = Intent(context, UpdateIntentService::class.java)
            context.startService(intent)
        }
    }
}
