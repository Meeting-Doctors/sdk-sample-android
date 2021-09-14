package com.meetingdoctors.chat.domain.repositories

import com.meetingdoctors.chat.data.repositories.CustomerResponseListener
import com.meetingdoctors.chat.data.webservices.entities.SendInvitationCodeBody
import com.meetingdoctors.chat.data.webservices.entities.SendInvitationCodeResponse
import com.meetingdoctors.chat.data.webservices.entities.UpdateProfileInfoBody
import com.meetingdoctors.chat.data.webservices.entities.UserDataResponse
import com.meetingdoctors.mdsecure.sharedpref.OnResetDataListener
import io.reactivex.Completable
import io.reactivex.Single

internal interface CustomerRepository {

    fun updateProfileInfo(userProfileInfoBody: UpdateProfileInfoBody, responseListener: CustomerResponseListener): Single<UserDataResponse>
    fun sendRelationShipInvitationCode(sendInvitationCodeBody: SendInvitationCodeBody, responseListener: CustomerResponseListener): Single<SendInvitationCodeResponse>
    fun expireJwt(onResetDataListener: OnResetDataListener?): Completable
}