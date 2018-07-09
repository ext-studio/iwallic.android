package com.iwallic.app.pages.user

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.iwallic.app.utils.LocaleUtils
import java.util.*
import android.content.Intent
import com.iwallic.app.base.MainActivity


class UserSettingLanguageActivity : BaseActivity() {
    var itemCN: LinearLayout? = null
    var itemEN: LinearLayout? = null
    var backBtn: LinearLayout? = null
    var saveBtn: LinearLayout? = null
    var itemCNChosen: ImageView? = null
    var itemENChosen: ImageView? = null
    var chosenLocale: Locale? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setting_language)
        this.initDOM()
        this.initClick()
        this.initLang()
    }

    private fun initDOM() {
        this.backBtn = findViewById<LinearLayout>(R.id.user_setting_lang_back)
        this.saveBtn = findViewById<LinearLayout>(R.id.user_setting_lang_save)
        this.itemCN = findViewById<LinearLayout>(R.id.user_setting_lang_cn)
        this.itemEN = findViewById<LinearLayout>(R.id.user_setting_lang_en)
        this.itemCNChosen = findViewById<ImageView>(R.id.user_setting_lang_cn_chosen)
        this.itemENChosen = findViewById<ImageView>(R.id.user_setting_lang_en_chosen)
    }

    private fun initClick() {
        this.backBtn!!.setOnClickListener{
            this.finish()
        }
        this.itemCN!!.setOnClickListener{
            this.chosenLocale = Locale.SIMPLIFIED_CHINESE
            this.itemCNChosen!!.visibility = View.VISIBLE
            this.itemENChosen!!.visibility = View.INVISIBLE
        }
        this.itemEN!!.setOnClickListener{
            this.chosenLocale = Locale.ENGLISH
            this.itemENChosen!!.visibility = View.VISIBLE
            this.itemCNChosen!!.visibility = View.INVISIBLE
        }
        this.saveBtn!!.setOnClickListener {
            if (this.chosenLocale == null) {
                return@setOnClickListener
            }
            if (this.chosenLocale == LocaleUtils.Current(this)) {
                this.finish()
                return@setOnClickListener
            } else {
                LocaleUtils.SetLocale(this, this.chosenLocale!!)
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                this.startActivity(intent);
            }
        }
    }

    private fun initLang() {
        this.chosenLocale = LocaleUtils.Current(this)
        when (this.chosenLocale) {
            Locale.SIMPLIFIED_CHINESE -> this.itemCNChosen!!.visibility = View.VISIBLE
            Locale.ENGLISH -> this.itemENChosen!!.visibility = View.VISIBLE
        }
    }
}
