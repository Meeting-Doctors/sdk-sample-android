package com.meetingdoctors.chat.domain.entitesextensions

import com.meetingdoctors.chat.domain.entities.UserData

internal fun UserData.isVideoCallOneToOneEnabled(): Boolean {

    return  features?.video_call_1to1 ?: false
}