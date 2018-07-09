package com.iwallic.app.navigator


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import com.iwallic.app.R
import com.iwallic.app.user.UserSettingActivity
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.WalletUtils
import com.iwallic.app.wallet.WalletActivity

class UserFragment : Fragment() {

    companion object {
        val TAG: String = UserFragment::class.java.simpleName
        fun newInstance() = UserFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        view.findViewById<LinearLayout>(R.id.fragment_user_setting).setOnClickListener {
            activity!!.startActivity(Intent(context, UserSettingActivity::class.java))
        }

        view.findViewById<LinearLayout>(R.id.fragment_user_signout).setOnClickListener {
            DialogUtils.Dialog(context!!, R.string.dialog_title_warn, R.string.dialog_content_signout, R.string.dialog_ok, R.string.dialog_no, fun (confirm: Boolean) {
                if (confirm) {
                    WalletUtils.close(context!!)
                    val intent = Intent(context, WalletActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    activity!!.finish()
                }
            })
        }
        return view
    }
}
