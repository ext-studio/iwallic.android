package com.iwallic.app.pages.user

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R

class UserSettingActivity : BaseActivity() {
    private lateinit var backTV: TextView
    private lateinit var skinFL: FrameLayout
    private lateinit var netFL: FrameLayout
    private lateinit var langFL: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setting)

        initDOM()
        initClick()
    }

    private fun initDOM() {
        backTV = findViewById(R.id.user_setting_back)
        langFL = findViewById(R.id.user_setting_lang)
        skinFL = findViewById(R.id.user_setting_skin)
        netFL = findViewById(R.id.user_setting_net)
    }

    private fun initClick() {
        backTV.setOnClickListener {
            finish()
        }
        langFL.setOnClickListener {
            startActivity(Intent(this, UserSettingLanguageActivity::class.java))
        }
        skinFL.setOnClickListener {
            startActivity(Intent(this, UserSettingThemeActivity::class.java))
        }
        netFL.setOnClickListener {
            startActivity(Intent(this, UserSettingNetActivity::class.java))
        }
    }
}
