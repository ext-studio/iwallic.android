package com.iwallic.app.pages.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.iwallic.app.pages.main.MainActivity
import com.iwallic.app.utils.SharedPrefUtils

class UserSettingThemeActivity : BaseActivity() {
    private lateinit var itemDefault: FrameLayout
    private lateinit var itemNight: FrameLayout
    private lateinit var backBtn: TextView
    private lateinit var saveBtn: TextView
    private lateinit var itemDefaultChosen: ImageView
    private lateinit var itemNightChosen: ImageView
    private var chosenSkin: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setting_theme)
        initDOM()
        initClick()
        initSkin()
    }

    private fun initDOM() {
        backBtn = findViewById(R.id.user_setting_skin_back)
        saveBtn = findViewById(R.id.user_setting_skin_save)
        itemDefault = findViewById(R.id.user_setting_skin_default)
        itemNight = findViewById(R.id.user_setting_skin_night)
        itemDefaultChosen = findViewById(R.id.user_setting_skin_default_chosen)
        itemNightChosen = findViewById(R.id.user_setting_skin_night_chosen)
    }

    private fun initClick() {
        backBtn.setOnClickListener{
            finish()
        }
        itemDefault.setOnClickListener{
            chosenSkin = "default"
            itemDefaultChosen.visibility = View.VISIBLE
            itemNightChosen.visibility = View.INVISIBLE
        }
        itemNight.setOnClickListener{
            chosenSkin = "night"
            itemNightChosen.visibility = View.VISIBLE
            itemDefaultChosen.visibility = View.INVISIBLE
        }
        saveBtn.setOnClickListener {
            if (chosenSkin == null) {
                return@setOnClickListener
            }
            if (chosenSkin == SharedPrefUtils.getSkin(this)) {
                finish()
                return@setOnClickListener
            } else {
                SharedPrefUtils.setSkin(this, chosenSkin!!)
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
    }

    private fun initSkin() {
        chosenSkin = SharedPrefUtils.getSkin(this)
        when (chosenSkin) {
            "default" -> itemDefaultChosen.visibility = View.VISIBLE
            "night" -> itemNightChosen.visibility = View.VISIBLE
        }
    }
}
