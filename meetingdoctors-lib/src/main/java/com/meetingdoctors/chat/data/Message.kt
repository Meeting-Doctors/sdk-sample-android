package com.meetingdoctors.chat.data

import androidx.annotation.Keep
import com.meetingdoctors.chat.data.MessageConstants.FILENAME_KEY
import com.meetingdoctors.chat.data.MessageConstants.FILESIZE_KEY
import com.meetingdoctors.chat.data.MessageConstants.FILE_KEY
import com.meetingdoctors.chat.data.MessageConstants.FILE_URL_KEY
import com.meetingdoctors.chat.data.MessageConstants.IMAGE_HEIGHT_KEY
import com.meetingdoctors.chat.data.MessageConstants.IMAGE_KEY
import com.meetingdoctors.chat.data.MessageConstants.IMAGE_URL_KEY
import com.meetingdoctors.chat.data.MessageConstants.IMAGE_WIDTH_KEY
import com.meetingdoctors.chat.data.MessageConstants.MESSAGE_ID_KEY
import com.meetingdoctors.chat.data.MessageConstants.STATUS_KEY
import com.meetingdoctors.chat.data.MessageConstants.STRING_KEY
import com.meetingdoctors.chat.data.MessageConstants.THUMB_URL_KEY
import com.meetingdoctors.chat.data.MessageConstants.TIME_KEY
import com.meetingdoctors.chat.data.MessageConstants.TYPE_KEY
import org.json.JSONObject


/**
 * Created by Héctor Manrique on 4/9/21.
 */

@Keep
class Message private constructor() {
    var type = 0
        private set
    var id: String? = null
        private set
    var time: Long? = null
    var status: Int? = null
    var string: String? = null
        private set
    var imageUrl: String? = null
    var thumbUrl: String? = null
    var imageWidth: Long? = null
    var imageHeight: Long? = null
    var fileSize: Long? = null
    var fileUrl: String? = null
    var fileName: String? = null

    @Keep
    class Builder {
        private var mType = 0
        private var mId: String? = null
        private var mTime: Long? = null
        private var mStatus: Int? = null
        private var mString: String? = null
        private var mImageUrl: String? = null
        private var mThumbUrl: String? = null
        private var mImageWidth: Long? = null
        private var mImageHeight: Long? = null
        private var mFileUrl: String? = null
        private var mFileName: String? = null
        private var mFileSize: Long? = null

        constructor(message: JSONObject, mine: Boolean) {
            mId = message.getString(MESSAGE_ID_KEY)
            mTime = message.getLong(TIME_KEY)
            mStatus = message.getInt(STATUS_KEY)
            when (message.getString(TYPE_KEY)) {
                STRING_KEY -> {
                    mType = if (mine) TYPE_STRING_MINE else TYPE_STRING_THEIR
                    mString = message.getString(STRING_KEY)
                }
                IMAGE_KEY -> {
                    mType = if (mine) TYPE_IMAGE_MINE else TYPE_IMAGE_THEIR
                    mImageUrl = message.getString(IMAGE_URL_KEY)
                    mThumbUrl = message.getString(THUMB_URL_KEY)
                    mImageWidth = message.getLong(IMAGE_WIDTH_KEY)
                    mImageHeight = message.getLong(IMAGE_HEIGHT_KEY)
                    mFileName = if (message.has(FILENAME_KEY)) message.getString(FILENAME_KEY) else null
                    mFileSize = message.getLong(FILESIZE_KEY)
                }
                FILE_KEY -> {
                    mType = if (mine) TYPE_FILE_MINE else TYPE_FILE_THEIR
                    mFileUrl = message.getString(FILE_URL_KEY)
                    mFileName = if (message.has(FILENAME_KEY)) message.getString(FILENAME_KEY) else null
                    mFileSize = message.getLong(FILESIZE_KEY)
                }
            }
        }

        constructor() {}

        fun type(type: Int): Builder {
            mType = type
            return this
        }

        fun id(id: String?): Builder {
            mId = id
            return this
        }

        fun time(time: Long?): Builder {
            mTime = time
            return this
        }

        fun status(status: Int?): Builder {
            mStatus = status
            return this
        }

        fun string(string: String?): Builder {
            mString = string
            return this
        }

        fun imageUrl(imageUrl: String?): Builder {
            mImageUrl = imageUrl
            return this
        }

        fun thumbUrl(thumbUrl: String?): Builder {
            mThumbUrl = thumbUrl
            return this
        }

        fun imageWidth(imageWidth: Long?): Builder {
            mImageWidth = imageWidth
            return this
        }

        fun imageHeight(imageHeight: Long?): Builder {
            mImageHeight = imageHeight
            return this
        }

        fun fileUrl(fileUrl: String?): Builder {
            mFileUrl = fileUrl
            return this
        }

        fun fileName(fileName: String?): Builder {
            mFileName = fileName
            return this
        }

        fun fileSize(fileSize: Long?): Builder {
            mFileSize = fileSize
            return this
        }

        fun build(): Message {
            val message = Message()
            message.type = mType
            message.id = mId
            message.time = mTime
            message.status = mStatus
            message.string = mString
            message.imageUrl = mImageUrl
            message.thumbUrl = mThumbUrl
            message.imageWidth = mImageWidth
            message.imageHeight = mImageHeight
            message.fileUrl = mFileUrl
            message.fileName = mFileName
            message.fileSize = mFileSize
            return message
        }
    }

    companion object {
        const val TYPE_STRING_MINE = 0
        const val TYPE_STRING_THEIR = 1
        const val TYPE_DATE = 2
        const val TYPE_TYPING = 3
        const val TYPE_IMAGE_MINE = 4
        const val TYPE_IMAGE_THEIR = 5
        const val TYPE_FILE_MINE = 6
        const val TYPE_FILE_THEIR = 7
        const val TYPE_ALERT = 8
    }
}
