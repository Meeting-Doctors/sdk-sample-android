package com.meetingdoctors.chat.views.extensions

import android.app.Activity
import android.app.ActivityManager
import android.content.Context

fun Activity.isRunningInForeground(activityClass: Class<out Activity>): Boolean {
    val activityManager = baseContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val tasks = activityManager.getRunningTasks(RUNNING_TASKS_MAX_VALUE)

    if (tasks[0].numActivities > 0 && activityClass.name.equals(tasks[0].topActivity?.className, false)) {
        return true
    }

    return false
}

private const val RUNNING_TASKS_MAX_VALUE = 1