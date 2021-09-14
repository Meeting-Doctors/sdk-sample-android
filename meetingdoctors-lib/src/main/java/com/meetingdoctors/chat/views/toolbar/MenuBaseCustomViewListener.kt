package com.meetingdoctors.chat.views.toolbar

import androidx.annotation.Keep

@Keep
interface MenuBaseCustomViewListener {
    fun onNavigationMenu() {}
    fun onBackPressedNavigation() {}
    fun onCustomAction() {}
}