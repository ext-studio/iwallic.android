package com.iwallic.app.pages.transaction

import android.os.Bundle
import android.view.WindowManager
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity

class TransactionReceiveActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        setContentView(R.layout.activity_transaction_receive)

    }
}
