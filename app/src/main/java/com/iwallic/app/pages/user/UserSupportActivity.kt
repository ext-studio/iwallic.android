package com.iwallic.app.pages.user

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity

class UserSupportActivity : BaseActivity() {
    private lateinit var backLL: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_support)

        initDOM()
        initClick()
    }

    private fun initDOM() {
        backLL = findViewById(R.id.user_support_back)
    }

    private fun initClick() {
        findViewById<LinearLayout>(R.id.activity_user_support_wallet_guide).setOnClickListener {
            Toast.makeText(this, R.string.error_incoming, Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.activity_user_support_transaction_guide).setOnClickListener {
            Toast.makeText(this, R.string.error_incoming, Toast.LENGTH_SHORT).show()
        }

        backLL.setOnClickListener {
            finish()
        }
    }
}
