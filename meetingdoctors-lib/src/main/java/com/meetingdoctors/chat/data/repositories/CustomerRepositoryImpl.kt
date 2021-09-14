package com.meetingdoctors.chat.data.repositories

import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.data.webservices.BEARER_PREFIX
import com.meetingdoctors.chat.data.webservices.endpoints.CustomerApi
import com.meetingdoctors.chat.data.webservices.entities.SendInvitationCodeBody
import com.meetingdoctors.chat.data.webservices.entities.SendInvitationCodeResponse
import com.meetingdoctors.chat.data.webservices.entities.UpdateProfileInfoBody
import com.meetingdoctors.chat.data.webservices.entities.UserDataResponse
import com.meetingdoctors.chat.domain.repositories.CustomerRepository
import com.meetingdoctors.mdsecure.sharedpref.OnResetDataListener
import io.reactivex.Completable
import io.reactivex.Single

internal class CustomerRepositoryImpl(private val customerApi: CustomerApi,
                                      private val repository: Repository): CustomerRepository {

    override fun updateProfileInfo(userProfileInfoBody: UpdateProfileInfoBody, responseListener: CustomerResponseListener): Single<UserDataResponse> =
            customerApi.updateProfileInfo(userProfileInfoBody)

    override fun sendRelationShipInvitationCode(sendInvitationCodeBody: SendInvitationCodeBody, responseListener: CustomerResponseListener): Single<SendInvitationCodeResponse>  =
            customerApi.sendRelationshipsInvitationCode(sendInvitationCodeBody)

    override fun expireJwt(onResetDataListener: OnResetDataListener?): Completable =
            customerApi.logout(BEARER_PREFIX + Repository.instance?.getUserData()?.jwt)
}

interface CustomerResponseListener {
    fun onSuccessResponse()
    fun onErrorResponse(error: String) {}
}