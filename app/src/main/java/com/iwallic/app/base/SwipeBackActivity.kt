package com.iwallic.app.base

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import com.iwallic.app.utils.CommonUtils
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

@SuppressLint("Registered")
open class SwipeBackActivity(private val linked: Boolean = false): AppCompatActivity() {
    private var currX = 0f
    private var prevent = true

    private var swipe: Int = 0
    private var swipeDisposable: Disposable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        swipe = intent.getIntExtra("swipe_id", 0)
        // todo separate to 100% -> 70% -> old page -30%
        window.decorView.translationX = CommonUtils.screenWidth.toFloat()
        animateEnter()
        CommonUtils.log("create to:$swipe")
    }
    override fun startActivity(intent: Intent?) {
        if (linked) {
            val newSwipe = SwipeBackActivity.push(
            (intent?.flags == Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) ||
                    (intent?.getBooleanExtra("swipe_reset", false) == true)
            )
            intent?.putExtra("swipe_id", newSwipe)
            swipeDisposable?.dispose()
            swipeDisposable = null
            swipeDisposable = SwipeBackActivity.stack[newSwipe]?.subscribe {
                when (it) {
                    -1f -> animateEnter()
                    -2f -> animateLeave()
                    else -> window.decorView.translationX = (it - CommonUtils.screenWidth) * 0.3f
                }
            }
        }
        super.startActivity(intent)
        overridePendingTransition(0, 0)
        animateLeave(true)
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
        if (linked) {
            val newSwipe = SwipeBackActivity.push(
                    (intent?.flags == Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) ||
                            (intent?.getBooleanExtra("swipe_reset", false) == true)
            )
            intent?.putExtra("swipe_id", newSwipe)
            swipeDisposable?.dispose()
            swipeDisposable = null
            swipeDisposable = SwipeBackActivity.stack[newSwipe]?.subscribe {
                when (it) {
                    -1f -> animateEnter()
                    -2f -> animateLeave()
                    else -> window.decorView.translationX = (it - CommonUtils.screenWidth) * 0.3f
                }
            }
        }
        super.startActivityForResult(intent, requestCode)
        overridePendingTransition(0, 0)
        // todo separate to [new page 70%] -> -30%
        animateLeave(true)
    }

    override fun finish() {
        if (swipe == 0) {
            super.finish()
        } else {
            CommonUtils.log("$swipe")
            SwipeBackActivity.complete(swipe)
            window.decorView.animate().apply {
                // todo separate to 0 -> 70% -> 100% -> finish
                x(CommonUtils.screenWidth.toFloat())
                duration = 200
                withEndAction {
                    super.finish()
                    overridePendingTransition(0, 0)
                }
                start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        swipeDisposable?.dispose()
        CommonUtils.log("destroy to $swipe")
    }

    // 执行打开动画 也就是从右侧滑进出现
    private fun animateEnter() {
        window.decorView.animate().apply {
            x(0f)
            duration = 200
            start()
        }
    }
    // 执行离开动画 也就是滑到左侧消失
    private fun animateLeave(_delay: Boolean = false) {
        window.decorView.animate().apply {
            if (_delay) {
                startDelay = 50
            }
            x(-CommonUtils.screenWidth*0.3f)
            duration = 200
            start()
        }
    }

    companion object {
        private var id = 0
        // 页面id与监听
        var stack = mutableMapOf<Int, PublishSubject<Float>>()

        fun push(reset: Boolean = false): Int {
            id = if (reset) 0 else (id + 1) % 987654321 // 防止累加id过大而循环使用，理论上不会重用，若真重用则会丢失前一订阅使前一联动失效
            stack[id] = PublishSubject.create()
            return id
        }
        fun next(id: Int, value: Float) {
            stack[id]?.onNext(value)
        }
        fun complete(id: Int) {
            stack[id]?.onNext(-1f)
            stack[id]?.onComplete()
            stack.remove(id)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || swipe == 0) {
            return super.dispatchTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.x < CommonUtils.dp2px(this, 25f)) {
                    prevent = false
                    currX = event.x
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (prevent) {
                    return super.dispatchTouchEvent(event)
                }
                val offset = event.x - currX
                window.decorView.translationX =  if (window.decorView.translationX + offset > 0) window.decorView.translationX + offset else 0f
                SwipeBackActivity.next(swipe, window.decorView.translationX)
                currX = event.x
                return true
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (!prevent) {
                    if (window.decorView.translationX > CommonUtils.screenWidth*0.4f) {
                        finish()
                    } else {
                        animateEnter()
                        SwipeBackActivity.next(swipe, -2f)
                    }
                    prevent = true
                    return true
                }
                return super.dispatchTouchEvent(event)
            }
        }
        return super.dispatchTouchEvent(event)
    }
}
