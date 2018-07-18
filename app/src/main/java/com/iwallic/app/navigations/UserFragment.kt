package com.iwallic.app.navigations

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import com.iwallic.app.R
import com.iwallic.app.pages.user.UserAboutActivity
import com.iwallic.app.pages.user.UserSettingActivity
import com.iwallic.app.pages.user.UserSupportActivity
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.WalletUtils
import com.iwallic.app.pages.wallet.WalletActivity
import com.iwallic.app.pages.wallet.WalletBackupActivity
import com.iwallic.app.states.AssetState

class UserFragment : Fragment() {
    private lateinit var backupLL: LinearLayout
    private lateinit var settingLL: LinearLayout
    private lateinit var supportLL: LinearLayout
    private lateinit var aboutLL: LinearLayout
    private lateinit var closeLL: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)
        initDOM(view)
        initClick()
        return view
    }

    private fun initDOM(view: View) {
        backupLL = view.findViewById(R.id.fragment_user_backup)
        settingLL = view.findViewById(R.id.fragment_user_setting)
        supportLL = view.findViewById(R.id.fragment_user_support)
        aboutLL = view.findViewById(R.id.fragment_user_about)
        closeLL = view.findViewById(R.id.fragment_user_signout)
    }

    private fun initClick() {
        backupLL.setOnClickListener {
            activity!!.startActivity(Intent(context, WalletBackupActivity::class.java))
        }
        settingLL.setOnClickListener {
            activity!!.startActivity(Intent(context, UserSettingActivity::class.java))
        }
        supportLL.setOnClickListener {
            activity!!.startActivity(Intent(context, UserSupportActivity::class.java))
        }
        aboutLL.setOnClickListener {
            activity!!.startActivity(Intent(context, UserAboutActivity::class.java))
        }
        closeLL.setOnClickListener {
            resolveSignOut()
        }
    }

    private fun resolveSignOut() {
        DialogUtils.confirm(context!!, R.string.dialog_title_warn, R.string.dialog_content_signout, R.string.dialog_ok, R.string.dialog_no).subscribe {
            if (it) {
                AssetState.clear()
                WalletUtils.close(context!!)
                val intent = Intent(context, WalletActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                activity!!.finish()
            }
        }
    }

    companion object {
        val TAG: String = UserFragment::class.java.simpleName
        fun newInstance() = UserFragment()
    }
}
