package com.meetingdoctors.sdksample.utils

import com.meetingdoctors.chat.data.webservices.CustomerSdkBuildMode
import com.meetingdoctors.sdksample.BuildConfig.SDKCHAT_TARGET_ENVIRONMENT
import com.meetingdoctors.sdksample.BuildConfig.VIDEOCALL_TARGET_ENVIRONMENT
import com.meetingdoctors.videocall.api.VideoCallBuildMode

object BuildModeHelper {
    fun setCustomerSdkEnvironment(targetEnvironment: String): CustomerSdkBuildMode {
        return when (targetEnvironment) {
            CustomerSdkBuildMode.DEV.toString() -> CustomerSdkBuildMode.DEV
            CustomerSdkBuildMode.STAGING.toString() -> CustomerSdkBuildMode.STAGING
            CustomerSdkBuildMode.PROD.toString() -> CustomerSdkBuildMode.PROD
            else -> throw IllegalStateException("Unexpected value: $SDKCHAT_TARGET_ENVIRONMENT")
        }
    }

    fun setVideocallSdkEnvironment(targetEnvironment: String): VideoCallBuildMode {
        return when (targetEnvironment) {
            VideoCallBuildMode.DEV.toString() -> VideoCallBuildMode.DEV
            VideoCallBuildMode.STAGING.toString() -> VideoCallBuildMode.STAGING
            VideoCallBuildMode.PROD.toString() -> VideoCallBuildMode.PROD
            else -> throw java.lang.IllegalStateException("Unexpected value: $VIDEOCALL_TARGET_ENVIRONMENT")
        }
    }
}