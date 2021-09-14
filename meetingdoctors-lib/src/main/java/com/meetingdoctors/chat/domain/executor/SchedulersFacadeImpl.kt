package com.meetingdoctors.chat.domain.executor

import androidx.annotation.Keep
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SchedulersFacadeImpl {

    @Keep
    companion object {

        var schedulersFacade: SchedulersFacade? = null
            private set

        fun getInstance(): SchedulersFacade? {
            if (schedulersFacade == null) {
                SchedulersFacadeImpl().init()
            }
            return schedulersFacade
        }
    }


    private fun init() {
        schedulersFacade = object: SchedulersFacade {
            override fun io(): Scheduler {
                return Schedulers.io();
            }

            override fun ui(): Scheduler {
                return AndroidSchedulers.mainThread();
            }
        }
    }


}