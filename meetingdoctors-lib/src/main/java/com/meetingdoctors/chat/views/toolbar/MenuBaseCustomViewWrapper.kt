package com.meetingdoctors.chat.views.toolbar

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.annotation.Keep
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.views.base.BaseTitleBar

class MenuBaseCustomViewWrapper(private val action: MenuBaseAction)  {
    private lateinit var customView: View
    private lateinit var customTitle: String
    private var menuBaseCustomViewListener: MenuBaseCustomViewListener? = null

    fun getCustomView(): View {
        return customView
    }

    fun setActionCallback(listener: MenuBaseCustomViewListener): MenuBaseCustomViewWrapper {
        menuBaseCustomViewListener = listener
        return this
    }

    fun saveCustomView(view: View): MenuBaseCustomViewWrapper {
        customView = view
        return this
    }

    fun saveCustomTitle(text: String): MenuBaseCustomViewWrapper {
        customTitle = text
        return this
    }

    fun setView(context: Context, titleBar: BaseTitleBar): MenuBaseCustomViewWrapper {
        when (action) {
            MenuBaseAction.NAVIGATION_MENU_ACTION -> {

                if (titleBar.findViewById<View>(R.id.meetingdoctors_menu_button) != null) {
                    titleBar.findViewById<View>(R.id.meetingdoctors_menu_button).setOnClickListener(View.OnClickListener {
                        openMenu(titleBar)
                        menuBaseCustomViewListener?.apply {
                            onNavigationMenu()
                        }
                    })
                }
            }

            MenuBaseAction.BACK_NAVIGATION_ACTION -> {
                val drawer = titleBar.rootView.findViewById<DrawerLayout>(R.id.meetingdoctors_drawer_layout)
                if (drawer != null) {
                    titleBar.findViewById<View>(R.id.meetingdoctors_menu_button).setOnClickListener(View.OnClickListener {
                        (context as Activity).onBackPressed()

                        menuBaseCustomViewListener?.apply {
                            onBackPressedNavigation()
                        }
                    });
                }
            }

            MenuBaseAction.CUSTOM_ACTION -> {}
        }

        return this
    }

    private fun openMenu(titleBar: BaseTitleBar) {
        val drawer = titleBar.rootView.findViewById(R.id.meetingdoctors_drawer_layout) as DrawerLayout
        drawer.openDrawer(GravityCompat.START)
    }
}

enum class MenuBaseAction {
    NAVIGATION_MENU_ACTION, BACK_NAVIGATION_ACTION, CUSTOM_ACTION
}