package com.meetingdoctors.chat.domain.usecase

import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.domain.executor.SchedulersFacade
import com.meetingdoctors.chat.domain.usecase.base.UseCase
import io.reactivex.Single

class AreReportsOptionEnabledUseCase(schedulersFacade: SchedulersFacade) :
        UseCase.RxSingleUseCase<Boolean, Unit>(schedulersFacade) {

    override fun build(params: Unit): Single<Boolean> {
        return Single.just(Repository.instance?.getUserData()?.features?.video_call ?: false)
    }

}