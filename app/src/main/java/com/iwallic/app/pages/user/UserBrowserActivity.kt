package com.iwallic.app.pages.user

import android.os.Bundle
import com.iwallic.app.R
import com.iwallic.app.base.BaseActivity

// 1. set title
// 2. if transfer url in, open given url
// 3. if no url given, open default local web page(same as find fragment)

class UserBrowserActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_browser)
    }
}
