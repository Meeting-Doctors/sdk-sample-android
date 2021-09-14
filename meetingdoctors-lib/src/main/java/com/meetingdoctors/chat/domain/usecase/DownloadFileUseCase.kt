package com.meetingdoctors.chat.domain.usecase

import com.meetingdoctors.chat.data.FileMimeType
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.domain.executor.SchedulersFacade
import com.meetingdoctors.chat.domain.usecase.base.UseCase
import io.reactivex.Completable

class DownloadFileUseCase (schedulersFacade: SchedulersFacade) :
        UseCase.RxCompletableUseCase<DownloadFileUseCase.Params>(schedulersFacade) {


    override fun build(params: Params): Completable {

        return Repository.instance!!.downloadAndStoreFile(params.fileUrl, params.fileName, params.fileMimeType)
    }

    data class Params(val fileUrl: String, val fileName: String, val fileMimeType: FileMimeType)

}