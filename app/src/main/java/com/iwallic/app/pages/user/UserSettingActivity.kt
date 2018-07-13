package com.iwallic.app.pages.user

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R

class UserSettingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setting)

        findViewById<LinearLayout>(R.id.user_setting_back).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.user_setting_lang).setOnClickListener {
            startActivity(Intent(this, UserSettingLanguageActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.user_setting_skin).setOnClickListener {
            startActivity(Intent(this, UserSettingThemeActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.user_setting_net).setOnClickListener {
            startActivity(Intent(this, UserSettingNetActivity::class.java))
        }
    }
}
