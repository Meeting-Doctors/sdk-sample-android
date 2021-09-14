package com.meetingdoctors.chat.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.views.base.BaseTitleBar

/**
 * Created by Héctor Manrique on 4/9/21.
 */
class MedicalHistoryListTitleBar @JvmOverloads constructor(
        context: Context,
        title: String?,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : BaseTitleBar(context, attrs, defStyleAttr) {

    init {
        init()
        setTitle(title)
    }

    private fun init() {
        View.inflate(context, R.layout.mediquo_layout_medical_history_list_title_bar, this)
    }

    override fun setTitle(title: String?) {
        (findViewById<View>(R.id.title) as TextView).text = title
    }

    fun hideAddButton() {
        findViewById<View>(R.id.add_button).visibility = View.GONE
    }
}
