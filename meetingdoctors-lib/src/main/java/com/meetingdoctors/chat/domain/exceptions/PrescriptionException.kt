package com.meetingdoctors.chat.domain.exceptions


sealed class PrescriptionException : Exception() {
    class PrescriptionNotFoundException : PrescriptionException()
}