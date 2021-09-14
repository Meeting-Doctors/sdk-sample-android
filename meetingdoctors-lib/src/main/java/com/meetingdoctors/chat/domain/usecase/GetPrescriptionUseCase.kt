package com.meetingdoctors.chat.domain.usecase

import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.domain.entities.Prescription
import com.meetingdoctors.chat.domain.executor.SchedulersFacade
import com.meetingdoctors.chat.domain.usecase.base.UseCase
import io.reactivex.Single

class GetPrescriptionUseCase(schedulersFacade: SchedulersFacade) :
        UseCase.RxSingleUseCase<Prescription, Unit>(schedulersFacade) {


    override fun build(params: Unit): Single<Prescription> {

        return Repository.instance!!.getPrescription()
    }

}