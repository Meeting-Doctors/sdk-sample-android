package com.meetingdoctors.chat.domain.executor

import io.reactivex.Scheduler

interface SchedulersFacade {

    fun io(): Scheduler

    fun ui(): Scheduler
}