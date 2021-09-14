package com.meetingdoctors.chat

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener

class PermissionManager  {

    @Keep
    companion object {
        var instance: PermissionManager? = null
            private set

        fun newInstance(): PermissionManager {
            if (instance == null) {
                instance = PermissionManager()
            }
            return instance!!
        }
    }

    fun setRequestPermissions(
            activity: Activity,
            permissions: ArrayList<String>,
            listener: MultiplePermissionsListener
    ) {

        Dexter.withActivity(activity)
                .withPermissions(permissions)
                .withListener(listener)
                .check()
    }

    fun setAllPermissionListener(
            context: Context,
            rootView: ViewGroup?,
            permissionMessage: String,
            function: () -> Unit
    ): CompositeMultiplePermissionsListener {

        val permissionListener = object : MultiplePermissionsListener {

            override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
            ) {
            }

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report != null && report.deniedPermissionResponses.size == 0) {
                    function()
                }
            }
        }

        val snackbarPermissionListener = SnackbarOnAnyDeniedMultiplePermissionsListener.Builder
                .with(rootView, permissionMessage)
                .withOpenSettingsButton(context.getString(R.string.meetingdoctors_settings))
                .withDuration(Snackbar.LENGTH_LONG)
                .build()

        return CompositeMultiplePermissionsListener(permissionListener, snackbarPermissionListener)
    }

    fun checkSomePermissionUngranted(context: Context, permissionToCheck: List<String>): Boolean {

        var somePermissionUngranted = false

        for (permission in permissionToCheck) {
            val permissionIsGranted = ContextCompat.checkSelfPermission(
                    context,
                    permission
            )
            if (permissionIsGranted != PackageManager.PERMISSION_GRANTED) {
                somePermissionUngranted = true
                break
            }
        }
        return somePermissionUngranted
    }
}
