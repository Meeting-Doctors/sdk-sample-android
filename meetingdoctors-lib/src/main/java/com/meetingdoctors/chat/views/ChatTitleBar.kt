package com.meetingdoctors.chat.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.views.base.BaseTitleBar

/**
 * Created by Héctor Manrique on 4/12/21.
 */

class ChatTitleBar @JvmOverloads constructor(
        context: Context,
        title: String?,
        subtitle: String?,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : BaseTitleBar(context, attrs, defStyleAttr) {

    init {
        init()
        setTitle(title)
        setSubtitle(subtitle)
    }
    private fun init() {
        View.inflate(context, R.layout.mediquo_layout_chat_title_bar, this)
    }

    override fun setTitle(title: String?) {
        (findViewById<View>(R.id.title) as TextView).text = title
    }

    override fun setSubtitle(subtitle: String?) {
        (findViewById<View>(R.id.subtitle) as TextView).text = subtitle
    }

    override fun setSubtitle(resourceId: Int) {
        (findViewById<View>(R.id.subtitle) as TextView).setText(resourceId)
    }
}
