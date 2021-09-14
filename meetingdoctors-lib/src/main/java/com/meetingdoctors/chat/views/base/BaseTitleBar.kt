package com.meetingdoctors.chat.views.base

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.meetingdoctors.chat.R

/**
 * Created by Héctor Manrique on 4/12/21.
 */

open class BaseTitleBar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        init()
    }

    private fun init() {}
    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        if (findViewById<View?>(R.id.back_button) != null) {
            findViewById<View>(R.id.back_button).setOnClickListener { (context as Activity).onBackPressed() }
        }
    }

    open fun setTitle(text: String?) {}
    open fun setTitle(resourceId: Int) {}
    open fun setSubtitle(text: String?) {}
    open fun setSubtitle(resourceId: Int) {}
}
