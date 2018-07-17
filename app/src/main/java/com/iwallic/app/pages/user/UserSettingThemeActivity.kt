package com.iwallic.app.pages.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.iwallic.app.base.MainActivity
import com.iwallic.app.utils.SharedPrefUtils

class UserSettingThemeActivity : BaseActivity() {
    private lateinit var itemDefault: LinearLayout
    private lateinit var itemNight: LinearLayout
    private lateinit var backBtn: LinearLayout
    private lateinit var saveBtn: LinearLayout
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
