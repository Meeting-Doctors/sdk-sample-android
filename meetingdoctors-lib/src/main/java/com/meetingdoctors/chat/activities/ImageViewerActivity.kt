package com.meetingdoctors.chat.activities

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.meetingdoctors.chat.BuildConfig
import com.meetingdoctors.chat.MeetingDoctorsClient.Companion.instance
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.BaseActivity
import com.meetingdoctors.chat.helpers.BitmapHelper.Companion.saveBitmapToTempFile
import kotlinx.android.synthetic.main.mediquo_activity_image_viewer.*
import java.io.File

/**
 * Created by Héctor Manrique on 4/15/21.
 */

class ImageViewerActivity : BaseActivity() {
    private var imageFile: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!BuildConfig.DEBUG) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE)
        }
        super.setContentView(R.layout.mediquo_activity_image_viewer)
        if(instance == null) {
            Log.e("MeetingDoctorsClient", "MeetingDoctorsClient not initialized")
            finish()
            return
        }


        val imagePath = intent.getStringExtra("imagePath")
        if(imagePath == null) {
            finish()
            return
        }

        if (imagePath!!.trim { it <= ' ' }.toLowerCase()?.startsWith("http")) {
            Glide.with(this).load(imagePath!!).into(target)
        } else {
            Glide.with(this).load(File(imagePath!!)).into(target)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imageFile?.delete()
    }

    private val target: CustomTarget<Drawable> = object : CustomTarget<Drawable>() {
        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
            try {
                imageFile = saveBitmapToTempFile(this@ImageViewerActivity, resource.toBitmap())
                image?.setImage(ImageSource.uri(Uri.fromFile(imageFile)))
                loading?.visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
                finish()
                return
            }
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            finish()
        }

        override fun onLoadCleared(placeholder: Drawable?) {

        }
    }
}
