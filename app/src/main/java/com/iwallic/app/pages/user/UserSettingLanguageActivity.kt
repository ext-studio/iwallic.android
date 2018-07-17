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
    private lateinit var itemCN: LinearLayout
    private lateinit var itemEN: LinearLayout
    private lateinit var backBtn: LinearLayout
    private lateinit var saveBtn: LinearLayout
    private lateinit var itemCNChosen: ImageView
    private lateinit var itemENChosen: ImageView
    var chosenLocale: Locale? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setting_language)
        initDOM()
        initClick()
        initLang()
    }

    private fun initDOM() {
        backBtn = findViewById(R.id.user_setting_lang_back)
        saveBtn = findViewById(R.id.user_setting_lang_save)
        itemCN = findViewById(R.id.user_setting_lang_cn)
        itemEN = findViewById(R.id.user_setting_lang_en)
        itemCNChosen = findViewById(R.id.user_setting_lang_cn_chosen)
        itemENChosen = findViewById(R.id.user_setting_lang_en_chosen)
    }

    private fun initClick() {
        backBtn.setOnClickListener{
            finish()
        }
        itemCN.setOnClickListener{
            chosenLocale = Locale.SIMPLIFIED_CHINESE
            itemCNChosen.visibility = View.VISIBLE
            itemENChosen.visibility = View.INVISIBLE
        }
        itemEN.setOnClickListener{
            chosenLocale = Locale.ENGLISH
            itemENChosen.visibility = View.VISIBLE
            itemCNChosen.visibility = View.INVISIBLE
        }
        saveBtn.setOnClickListener {
            if (chosenLocale == null) {
                return@setOnClickListener
            }
            if (chosenLocale == LocaleUtils.Current(this)) {
                finish()
                return@setOnClickListener
            } else {
                LocaleUtils.SetLocale(this, chosenLocale!!)
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                this.startActivity(intent);
            }
        }
    }

    private fun initLang() {
        chosenLocale = LocaleUtils.Current(this)
        when (chosenLocale) {
            Locale.SIMPLIFIED_CHINESE -> itemCNChosen.visibility = View.VISIBLE
            Locale.ENGLISH -> itemENChosen.visibility = View.VISIBLE
        }
    }
}
