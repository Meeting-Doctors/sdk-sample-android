package com.meetingdoctors.chat.domain.repositories

import com.meetingdoctors.chat.data.webservices.entities.PrescriptionResponse
import io.reactivex.Single

interface PrescriptionRepository {

     fun getPrescription(): Single<PrescriptionResponse>

}