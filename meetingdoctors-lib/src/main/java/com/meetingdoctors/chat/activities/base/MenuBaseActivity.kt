package com.meetingdoctors.chat.activities.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.meetingdoctors.chat.MeetingDoctorsClient.Companion.instance
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.views.base.BaseTitleBar
import kotlinx.android.synthetic.main.meetingdoctors_activity_base_drawer.*

/**
 * Created by Héctor Manrique on 4/15/21.
 */

open class MenuBaseActivity : BaseActivity() {
    private var mTitleBar: BaseTitleBar? = null

    fun setTitleBar(titleBar: BaseTitleBar?) {
        mTitleBar = titleBar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.meetingdoctors_activity_base_drawer)
        val toggle = ActionBarDrawerToggle(this,
                meetingdoctors_drawer_layout,
                Toolbar(this),
                R.string.meetingdoctors_navigation_drawer_open,
                R.string.meetingdoctors_navigation_drawer_close)
        meetingdoctors_drawer_layout?.apply {
            addDrawerListener(toggle)
            setScrimColor(ContextCompat.getColor(context,R.color.meetingdoctorsBlueTranslucent))
        }
        toggle.syncState()
        val menuView = instance?.getMenuView()
        if (menuView != null) {
            meetingdoctors_navigation_view?.addView(menuView)
        } else {
            meetingdoctors_drawer_layout?.apply {
                setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }

    }

    override fun setContentView(contentResourceId: Int) {
        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mTitleBar?.let {
            meetingdoctors_title_bar?.addView(mTitleBar)
        }

        val content = layoutInflater.inflate(contentResourceId, null, false)
        meetingdoctors_activity_content?.addView(content)
    }

    override fun onPause() {
        super.onPause()
        closeMenu()
    }

    fun closeMenu() {
        if (meetingdoctors_drawer_layout?.isDrawerOpen(GravityCompat.START) == true) {
            meetingdoctors_drawer_layout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (meetingdoctors_drawer_layout?.isDrawerOpen(GravityCompat.START) == true) {
            meetingdoctors_drawer_layout?.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
