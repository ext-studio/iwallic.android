package com.iwallic.app.pages.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.pages.main.MainActivity
import com.iwallic.app.states.AssetState
import com.iwallic.app.states.BlockState
import com.iwallic.app.states.TransactionState
import com.iwallic.app.utils.ACache
import com.iwallic.app.utils.CommonUtils
import com.iwallic.app.utils.SharedPrefUtils

class UserSettingNetActivity : BaseActivity() {
    private lateinit var itemMain: FrameLayout
    private lateinit var itemTest: FrameLayout
    private lateinit var backBtn: TextView
    private lateinit var saveBtn: TextView
    private lateinit var itemMainChosen: ImageView
    private lateinit var itemTestChosen: ImageView
    private var chosenNet: String = "main"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setting_net)
        initDOM()
        initClick()
        initNet()
    }

    private fun initDOM() {
        backBtn = findViewById(R.id.user_setting_net_back)
        saveBtn = findViewById(R.id.user_setting_net_save)
        itemMain = findViewById(R.id.user_setting_net_main)
        itemTest = findViewById(R.id.user_setting_net_test)
        itemMainChosen = findViewById(R.id.user_setting_net_main_chosen)
        itemTestChosen = findViewById(R.id.user_setting_net_test_chosen)
        itemTest.visibility = View.GONE
    }

    private fun initClick() {
        backBtn.setOnClickListener{
            finish()
        }
        itemMain.setOnClickListener{
            chosenNet = "main"
            itemMainChosen.visibility = View.VISIBLE
            itemTestChosen.visibility = View.INVISIBLE
        }
        itemTest.setOnClickListener{
            chosenNet = "test"
            itemTestChosen.visibility = View.VISIBLE
            itemMainChosen.visibility = View.INVISIBLE
        }
        saveBtn.setOnClickListener {
            if (chosenNet == SharedPrefUtils.getNet(this)) {
                finish()
                return@setOnClickListener
            } else {
                SharedPrefUtils.setNet(this, chosenNet)
                AssetState.cached = null
                ACache.get(this).clear()
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    private fun initNet() {
        chosenNet = SharedPrefUtils.getNet(this)
        when (chosenNet) {
            "main" -> itemMainChosen.visibility = View.VISIBLE
            "test" -> itemTestChosen.visibility = View.VISIBLE
        }
    }
}
