@file:JvmName("Constants")

package com.meetingdoctors.chat.data.webservices

import androidx.annotation.Keep

fun getChatServer(customerSdkBuildMode: CustomerSdkBuildMode): String {
    return when (customerSdkBuildMode) {
        CustomerSdkBuildMode.DEV -> "https://chat.dev.meetingdoctors.com/"
        CustomerSdkBuildMode.STAGING -> "https://chat.staging.meetingdoctors.com/"
        CustomerSdkBuildMode.PROD -> "https://chat.meetingdoctors.com/"
    }
}

fun getCustomerServer(customerSdkBuildMode: CustomerSdkBuildMode): String {
    return when (customerSdkBuildMode) {
        CustomerSdkBuildMode.DEV -> "https://customer.dev.meetingdoctors.com/"
        CustomerSdkBuildMode.STAGING -> "https://customer.staging.meetingdoctors.com/"
        CustomerSdkBuildMode.PROD -> "https://customer.meetingdoctors.com/"
    }
}

fun geProfessionalServer(customerSdkBuildMode: CustomerSdkBuildMode): String {
    return when (customerSdkBuildMode) {
        CustomerSdkBuildMode.DEV -> "https://professional.dev.meetingdoctors.com/api/"
        CustomerSdkBuildMode.STAGING -> "https://professional.staging.meetingdoctors.com/api/"
        CustomerSdkBuildMode.PROD -> "https://professional.meetingdoctors.com/api/"
    }
}

fun getConsultationsServer(customerSdkBuildMode: CustomerSdkBuildMode): String {
    return when (customerSdkBuildMode) {
        CustomerSdkBuildMode.DEV -> "https://consultations.dev.meetingdoctors.com/api/"
        CustomerSdkBuildMode.STAGING -> "https://consultations.staging.meetingdoctors.com/api/"
        CustomerSdkBuildMode.PROD -> "https://consultations.meetingdoctors.com/api/"
    }
}

fun getConsultationsCustomerServer(customerSdkBuildMode: CustomerSdkBuildMode): String {
    return when (customerSdkBuildMode) {
        CustomerSdkBuildMode.DEV -> "https://consultations.dev.meetingdoctors.com/customers/v1/medical-history/"
        CustomerSdkBuildMode.STAGING -> "https://consultations.staging.meetingdoctors.com/customers/v1/medical-history/"
        CustomerSdkBuildMode.PROD -> "https://consultations.meetingdoctors.com/customers/v1/medical-history/"
    }
}

fun getNotificationsServer(customerSdkBuildMode: CustomerSdkBuildMode): String {
    return when (customerSdkBuildMode) {
        CustomerSdkBuildMode.DEV -> "https://notifications.dev.meetingdoctors.com/api/v1/push/"
        CustomerSdkBuildMode.STAGING -> "https://notifications.staging.meetingdoctors.com/api/v1/push/"
        CustomerSdkBuildMode.PROD -> "https://notifications.meetingdoctors.com/api/v1/push/"
    }
}


fun getPrescriptionServer(customerSdkBuildMode: CustomerSdkBuildMode): String {
    return when (customerSdkBuildMode) {
        CustomerSdkBuildMode.DEV -> "https://electronic-prescription.dev.meetingdoctors.com/customer/v1/"
        CustomerSdkBuildMode.STAGING -> "https://electronic-prescription.staging.meetingdoctors.com/customer/v1/"
        CustomerSdkBuildMode.PROD -> "https://electronic-prescription.meetingdoctors.com/customer/v1/"
    }
}

@Keep
enum class CustomerSdkBuildMode {
    DEV, STAGING, PROD
}

const val BEARER_PREFIX = "Bearer "