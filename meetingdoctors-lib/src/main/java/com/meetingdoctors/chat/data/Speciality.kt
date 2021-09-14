package com.meetingdoctors.chat.data

import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.data.SpecialityInfo.Companion.getFlagFromName
import com.meetingdoctors.chat.data.SpecialityInfo.Companion.getIdFromName
import com.meetingdoctors.chat.data.SpecialityInfo.Companion.getNameFromId

/**
 * Created by Héctor Manrique on 4/8/21.
 */

enum class Speciality(speciality: Int) {

    GENERAL_MEDICINE(1),
    PEDIATRICS(2),
    PSYCHOLOGY(3),
    SPORTS_MEDICINE(4),
    CUSTOMER_CARE(8),
    MEDICAL_SUPPORT(9),
    PERSONAL_TRAINING(12),
    COMMERCIAL(13),
    MEDICAL_APPOINTMENT(14),
    CARDIOLOGY(15),
    GYNECOLOGY(16),
    PHARMACY(17),
    SEXOLOGY(18),
    NUTRITION(20),
    FERTILITY_CONSULTANT(21),
    NURSING(22),
    MEDICAL_ADVISOR(24),
    CUSTOMER_CARE_ISALUD(61),
    VETERINARY(62),
    DOCTOR_GO_HEALTH_ADVISOR(66),
    FITNESS_COACHING(67),
    NUTRITIONAL_COACHING(68);

    private val speciality: Int = speciality

    fun value(): Int {
        return speciality
    }

    companion object {
        fun name(id: Int?): String {
            return getNameFromId(id)
        }

        fun id(name: String?): Int {
            return getIdFromName(name!!)
        }

        fun flag(name: String?): Int {
            return getFlagFromName(name!!)
        }

        fun icon(id: Int): Int {
            return when (id) {
                16 -> R.drawable.mediquo_medico_icon
                3 -> R.drawable.mediquo_psicologo_icon
                8 -> R.drawable.mediquo_att_cliente_icon
                12 -> R.drawable.mediquo_ep_icon
                13 -> R.drawable.mediquo_att_cliente_icon
                14 -> R.drawable.mediquo_pedir_cita_icon
                17 -> R.drawable.mediquo_envio_med_icon
                18 -> R.drawable.mediquo_psicologo_icon
                20 -> R.drawable.mediquo_nutricion_icon
                21 -> R.drawable.mediquo_fertility_consultant_icon
                22 -> R.drawable.mediquo_nursing_icon
                24 -> R.drawable.mediquo_medico_icon
                61 -> R.drawable.mediquo_att_cliente_icon
                62 -> R.drawable.mediquo_veterinary_icon
                66 -> R.drawable.mediquo_psicologo_icon
                67 -> R.drawable.mediquo_nutricion_icon
                68 -> R.drawable.mediquo_ep_icon
                else -> R.drawable.mediquo_medico_icon
            }
        }
    }

}
