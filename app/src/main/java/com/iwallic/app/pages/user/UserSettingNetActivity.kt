package com.iwallic.app.pages.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.base.MainActivity
import com.iwallic.app.states.AssetState
import com.iwallic.app.states.BlockState
import com.iwallic.app.states.TransactionState
import com.iwallic.app.utils.ConfigUtils
import com.iwallic.app.utils.SharedPrefUtils

class UserSettingNetActivity : BaseActivity() {
    private lateinit var itemMain: LinearLayout
    private lateinit var itemTest: LinearLayout
    private lateinit var backBtn: LinearLayout
    private lateinit var saveBtn: LinearLayout
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
                AssetState.clear()
                TransactionState.clear()
                BlockState.clear()
                ConfigUtils.set(chosenNet)
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
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
