package com.iwallic.app.pages.user

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R

class UserSettingActivity : BaseActivity() {
    private lateinit var backLL: LinearLayout
    private lateinit var skinLL: LinearLayout
    private lateinit var netLL: LinearLayout
    private lateinit var langLL: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setting)

        initDOM()
        initClick()
    }

    private fun initDOM() {
        backLL = findViewById(R.id.user_setting_back)
        langLL = findViewById(R.id.user_setting_lang)
        skinLL = findViewById(R.id.user_setting_skin)
        netLL = findViewById(R.id.user_setting_net)
    }

    private fun initClick() {
        backLL.setOnClickListener {
            finish()
        }
        langLL.setOnClickListener {
            startActivity(Intent(this, UserSettingLanguageActivity::class.java))
        }
        skinLL.setOnClickListener {
            startActivity(Intent(this, UserSettingThemeActivity::class.java))
        }
        netLL.setOnClickListener {
            startActivity(Intent(this, UserSettingNetActivity::class.java))
        }
    }
}
