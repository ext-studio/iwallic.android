package com.iwallic.app.pages.wallet

import android.os.Bundle
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.base.BaseAuthActivity

class WalletVerifyActivity : BaseAuthActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_verify)
    }
}
