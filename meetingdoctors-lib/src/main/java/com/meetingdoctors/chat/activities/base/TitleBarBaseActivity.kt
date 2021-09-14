package com.meetingdoctors.chat.activities.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import com.meetingdoctors.chat.BuildConfig
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.views.base.BaseTitleBar
import kotlinx.android.synthetic.main.mediquo_activity_base.*

/**
 * Created by Héctor Manrique on 4/15/21.
 */

open class TitleBarBaseActivity : BaseActivity() {
    private var mTitleBar: BaseTitleBar? = null
    fun setTitleBar(titleBar: BaseTitleBar?) {
        mTitleBar = titleBar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!BuildConfig.DEBUG) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE)
        }
        super.setContentView(R.layout.mediquo_activity_base)
    }

    override fun setContentView(contentResourceId: Int) {
        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (mTitleBar != null) {
            title_bar?.addView(mTitleBar)
        }
        val content = layoutInflater.inflate(contentResourceId, null, false)
        activity_content?.addView(content)
    }

    fun setTitle(text: String?) {
        mTitleBar?.setTitle(text)
    }

    override fun setTitle(resourceId: Int) {
        mTitleBar?.setTitle(resourceId)

    }

    fun setSubtitle(text: String?) {
        mTitleBar?.setSubtitle(text)
    }

    fun setSubtitle(resourceId: Int) {
        mTitleBar?.setSubtitle(resourceId)
    }
}
