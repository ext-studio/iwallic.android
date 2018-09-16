package com.iwallic.app.pages.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import com.iwallic.app.R
import com.iwallic.app.base.BaseFragment
import com.iwallic.app.pages.user.UserAboutActivity
import com.iwallic.app.pages.user.UserSettingActivity
import com.iwallic.app.pages.user.UserSupportActivity
import com.iwallic.app.utils.DialogUtils
import com.iwallic.app.utils.WalletUtils
import com.iwallic.app.pages.wallet.WalletGuardActivity
import com.iwallic.app.pages.wallet.WalletBackupActivity
import com.iwallic.app.states.AssetManageState
import com.iwallic.app.states.AssetState

class UserFragment : BaseFragment() {
    private lateinit var backupLL: FrameLayout
    private lateinit var settingLL: FrameLayout
    private lateinit var supportLL: FrameLayout
    private lateinit var aboutLL: FrameLayout
    private lateinit var closeLL: FrameLayout

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
            context?.startActivity(Intent(context, WalletBackupActivity::class.java))
        }
        settingLL.setOnClickListener {
            context?.startActivity(Intent(context, UserSettingActivity::class.java))
        }
        supportLL.setOnClickListener {
            context?.startActivity(Intent(context, UserSupportActivity::class.java))
        }
        aboutLL.setOnClickListener {
            context?.startActivity(Intent(context, UserAboutActivity::class.java))
        }
        closeLL.setOnClickListener {
            resolveSignOut()
        }
    }

    private fun resolveSignOut() {
        DialogUtils.confirm(context!!, R.string.dialog_content_signout, R.string.dialog_title_warn, R.string.dialog_ok, R.string.dialog_no).subscribe {
            if (it) {
                AssetState.clear()
                AssetManageState.clear()
                WalletUtils.close(context!!)
                val intent = Intent(context, WalletGuardActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context?.startActivity(intent)
            }
        }
    }
}
