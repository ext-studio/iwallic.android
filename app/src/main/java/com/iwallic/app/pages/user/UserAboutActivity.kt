package com.iwallic.app.pages.user

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity

class UserAboutActivity : BaseActivity() {
    private lateinit var backLL: LinearLayout
    private lateinit var disclamerLL: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_about)

        initDOM()
        initClick()
    }

    private fun initDOM() {
        backLL = findViewById(R.id.user_about_back)
        disclamerLL = findViewById(R.id.activity_user_about_disclaimer)
    }

    private fun initClick() {
        disclamerLL.setOnClickListener {
            Toast.makeText(this, R.string.error_incoming, Toast.LENGTH_SHORT).show()
        }

        backLL.setOnClickListener {
            finish()
        }
    }
}
