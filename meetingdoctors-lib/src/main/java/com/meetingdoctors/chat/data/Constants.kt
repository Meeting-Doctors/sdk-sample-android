package com.meetingdoctors.chat.data


/**
 * Created by Héctor Manrique on 4/8/21.
 */
object Constants {
    const val CONNECTION_TIMEOUT = 30000L
    const val READ_TIMEOUT = 30000L
    const val WRITE_TIMEOUT = 60000L
    const val MAX_CACHE_SIZE_IN_BYTES: Long = 10000000
    const val CHAT_PAGE_SIZE = 100
    const val USER_STATUS_ANONYMOUS = 0
    const val USER_STATUS_IN_PROCESS = 1
    const val USER_STATUS_ACTIVE = 2
    const val USER_STATUS_NOT_ACTIVE = 3
    const val CHAT_SOCKET_WATCHDOG_FREQUENCY = 30000
    const val SOCKETIO_RECONNECTION_DELAY = 5000
}