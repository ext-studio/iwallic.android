package com.iwallic.app.base

abstract class BaseLazyFragment : BaseFragment() {
    private var showed = false
    private var created = false
    private var should = true

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        showed = isVisibleToUser
        if (showed && created) {
            if (should) {
                onResolve()
                should = false
            }
            onShown()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        should = true
        showed = false
        created = false
    }

    fun notifyCreatedView() {
        created = true
        if (showed && created && should) {
            onResolve()
            should = false
        }
    }

    open fun onShown() {}
    open fun onResolve() {}
}
