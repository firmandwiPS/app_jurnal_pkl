package com.ioreum.app_jurnal_pkl

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2

class CustomViewPager2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    lateinit var viewPager2: ViewPager2
    var disabledIndex = -1

    override fun onFinishInflate() {
        super.onFinishInflate()
        viewPager2 = getChildAt(0) as ViewPager2
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (viewPager2.currentItem == disabledIndex) false
        else super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (viewPager2.currentItem == disabledIndex) false
        else super.onTouchEvent(ev)
    }
}
