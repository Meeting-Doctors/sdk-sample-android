package com.meetingdoctors.chat.views.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.net.ServerInterface

/**
 * Created by Héctor Manrique on 4/12/21.
 */

open class BaseLinearLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    private lateinit var authenticateBroadcastReceiver: AuthenticateBroadcastReceiver
    private lateinit var deauthenticateBroadcastReceiver: DeauthenticateBroadcastReceiver
    var resumed = false
    var authenticated = false

    init {
        initialize()
    }

    private fun initialize() {
        authenticateBroadcastReceiver = AuthenticateBroadcastReceiver()
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(authenticateBroadcastReceiver,
                        IntentFilter(context.getString(R.string.meetingdoctors_local_broadcast_authenticate)
                        ))
        deauthenticateBroadcastReceiver = DeauthenticateBroadcastReceiver()
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(deauthenticateBroadcastReceiver,
                        IntentFilter(context.getString(R.string.meetingdoctors_local_broadcast_deauthenticate)
                        ))
    }

    override fun onAttachedToWindow() {
        Log.d("BaseLinearLayout", "onAttachedToWindow()")
        super.onAttachedToWindow()
        post(object : Runnable {
            override fun run() {
                if (isAttachedToWindow) {
                    if (isShown && !resumed) {
                        onResume()
                    } else if (!isShown && resumed) {
                        onPause()
                    }
                    postDelayed(this, 100)
                }
            }
        })
    }

    override fun onDetachedFromWindow() {
        Log.d("BaseLinearLayout", "onDetachedFromWindow()")
        super.onDetachedFromWindow()
        onPause()
    }

    protected fun onResume() {
        Log.d("BaseLinearLayout", "onResume()")
        resumed = true
        Repository.instance?.initialize(object : ServerInterface.ResponseListener {
            override fun onResponse(error: Throwable?, statusCode: Int, data: Any?) {
                if (this@BaseLinearLayout.isAttachedToWindow) {
                    authenticated = Repository.instance?.isAuthenticated() ?: false
                    onReady()
                }
            }
        })
    }

    open fun onPause() {
        Log.d("BaseLinearLayout", "onPause()")
        resumed = false
    }

    open fun onReady() {
        Log.d("BaseLinearLayout", "onReady()")
    }

    private inner class AuthenticateBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("BaseLinearLayout", "AuthenticateBroadcastReceiver.onReceive()")
            if (isAttachedToWindow && isShown && !authenticated) {
                onPause()
                onResume()
            }
        }
    }

    private inner class DeauthenticateBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("BaseLinearLayout", "DeauthenticateBroadcastReceiver.onReceive()")
            authenticated = false
            onPause()
            onResume()
        }
    }
}
