package com.meetingdoctors.chat.domain.repositories

import com.meetingdoctors.chat.data.FileMimeType
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody

interface FileRepository {

     fun getFile(fileUrl: String): Single<ResponseBody>

     fun writeResponseBodyToDisk(body: ResponseBody, fileName: String, fileMimeType: FileMimeType): Completable

}