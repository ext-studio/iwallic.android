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
    var itemDefault: LinearLayout? = null
    var itemNight: LinearLayout? = null
    var backBtn: LinearLayout? = null
    var saveBtn: LinearLayout? = null
    var itemDefaultChosen: ImageView? = null
    var itemNightChosen: ImageView? = null
    var chosenSkin: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setting_theme)
        this.initDOM()
        this.initClick()
        this.initSkin()
    }

    private fun initDOM() {
        this.backBtn = findViewById<LinearLayout>(R.id.user_setting_skin_back)
        this.saveBtn = findViewById<LinearLayout>(R.id.user_setting_skin_save)
        this.itemDefault = findViewById<LinearLayout>(R.id.user_setting_skin_default)
        this.itemNight = findViewById<LinearLayout>(R.id.user_setting_skin_night)
        this.itemDefaultChosen = findViewById<ImageView>(R.id.user_setting_skin_default_chosen)
        this.itemNightChosen = findViewById<ImageView>(R.id.user_setting_skin_night_chosen)
    }

    private fun initClick() {
        this.backBtn!!.setOnClickListener{
            finish()
        }
        this.itemDefault!!.setOnClickListener{
            this.chosenSkin = "default"
            this.itemDefaultChosen!!.visibility = View.VISIBLE
            this.itemNightChosen!!.visibility = View.INVISIBLE
        }
        this.itemNight!!.setOnClickListener{
            this.chosenSkin = "night"
            this.itemNightChosen!!.visibility = View.VISIBLE
            this.itemDefaultChosen!!.visibility = View.INVISIBLE
        }
        this.saveBtn!!.setOnClickListener {
            if (this.chosenSkin == null) {
                return@setOnClickListener
            }
            if (this.chosenSkin == SharedPrefUtils.getSkin(this)) {
                this.finish()
                return@setOnClickListener
            } else {
                SharedPrefUtils.setSkin(this, chosenSkin!!)
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                this.startActivity(intent);
            }
        }
    }

    private fun initSkin() {
        this.chosenSkin = SharedPrefUtils.getSkin(this)
        when (chosenSkin) {
            "default" -> itemDefaultChosen!!.visibility = View.VISIBLE
            "night" -> itemNightChosen!!.visibility = View.VISIBLE
        }
    }
}
