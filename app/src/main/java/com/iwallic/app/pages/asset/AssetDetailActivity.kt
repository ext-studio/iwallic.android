package com.iwallic.app.pages.asset

import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.iwallic.app.base.BaseActivity
import com.iwallic.app.R
import com.iwallic.app.models.addrassets
import com.iwallic.app.states.AssetState

class AssetDetailActivity : BaseActivity() {
    private lateinit var detailTV: TextView
    private lateinit var asset: addrassets

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset_detail)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)

        val assetId = intent.getStringExtra("asset")
        if (assetId.isEmpty()) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val tryGet = AssetState.get(assetId)
        if (tryGet == null) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        asset = tryGet
        detailTV = findViewById(R.id.asset_detail)
        detailTV.text = asset.symbol
    }
}
