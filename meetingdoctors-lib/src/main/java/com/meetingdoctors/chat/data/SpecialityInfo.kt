package com.meetingdoctors.chat.data

enum class SpecialityInfo(val specialityName: String, val flag: Int, val id: Int) {

    GENERAL_MEDICINE_INFO("generalMedicine", 1, 1),
    PEDIATRICS_INFO("pediatrics", 2, 2),
    PSYCHOLOGY_INFO("psychology", 4, 3),
    SPORTS_MEDICINE_INFO("sportsMedicine", 8, 4),
    CUSTOMER_CARE_INFO("customerCare", 16, 8),
    MEDICAL_SUPPORT_INFO("medicalSupport", 32, 9),
    PERSONAL_TRAINING_INFO("personalTraining", 64, 12),
    COMMERCIAL_INFO("commercial", 128, 13),
    MEDICAL_APPOINTMENT_INFO("medicalAppointment", 256, 14),
    CARDIOLOGY_INFO("cardiology", 512, 15),
    GYNECOLOGY_INFO("gynecology", 1024, 16),
    PHARMACY_INFO("pharmacy", 2048, 17),
    SEXOLOGY_INFO("sexology", 4096, 18),
    NUTRITION_INFO("nutrition", 8192, 20),
    FERTILITY_CONSULTANT_INFO("fertilityConsultant", 16384, 21),
    NURSING_INFO("nursing", 32768, 22),
    MEDICAL_ADVISOR_INFO("healthAdvisor", 65536, 24),
    CUSTOMER_CARE_ISALUD_INFO("customerCareISaludInfo", 131072, 61),
    VETERINARY("veterinary", 262144, 62),
    DOCTOR_GO_HEALTH_ADVISOR("doctorGoHealthAdvisor", 524288, 66),
    FITNESS_COACHING("fitnessCoaching", 1048576, 67),
    NUTRITIONAL_COACHING("nutritionalCoaching", 2097152, 68);

    companion object {

        fun getNameFromId(id: Int?): String {
            val speciality = values().firstOrNull { it.id == id }
            return speciality?.specialityName ?: "$id"
        }

        fun getIdFromName(name: String): Int {
            val speciality = values().firstOrNull { it.specialityName == name }
            return speciality?.id ?: 0
        }

        fun getFlagFromName(name: String): Int {
            val speciality = values().firstOrNull { it.specialityName == name }
            return speciality?.flag ?: 0
        }
    }
}