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
        this.initDOM()
        this.initClick()
        this.initNet()
    }

    private fun initDOM() {
        this.backBtn = findViewById(R.id.user_setting_net_back)
        this.saveBtn = findViewById(R.id.user_setting_net_save)
        this.itemMain = findViewById(R.id.user_setting_net_main)
        this.itemTest = findViewById(R.id.user_setting_net_test)
        this.itemMainChosen = findViewById(R.id.user_setting_net_main_chosen)
        this.itemTestChosen = findViewById(R.id.user_setting_net_test_chosen)
    }

    private fun initClick() {
        this.backBtn.setOnClickListener{
            this.finish()
        }
        this.itemMain.setOnClickListener{
            this.chosenNet = "main"
            this.itemMainChosen.visibility = View.VISIBLE
            this.itemTestChosen.visibility = View.INVISIBLE
        }
        this.itemTest.setOnClickListener{
            this.chosenNet = "test"
            this.itemTestChosen.visibility = View.VISIBLE
            this.itemMainChosen.visibility = View.INVISIBLE
        }
        this.saveBtn.setOnClickListener {
            if (this.chosenNet == SharedPrefUtils.getNet(this)) {
                this.finish()
                return@setOnClickListener
            } else {
                SharedPrefUtils.setNet(this, chosenNet)
                AssetState.clear()
                TransactionState.clear()
                BlockState.clear()
                ConfigUtils.set(chosenNet)
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                this.startActivity(intent)
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
