package com.meetingdoctors.chat.data.webservices.mappers

import com.meetingdoctors.chat.UnitTestConstants.patientTestHash
import com.meetingdoctors.chat.data.webservices.entities.ExternalAllergy
import com.meetingdoctors.chat.domain.entities.Allergy
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object AllergyMapperSpekTest : Spek({

    lateinit var allergyMapper: AllergyMapper
    lateinit var sampleAllergy: Allergy
    lateinit var sampleExternalAllergy: ExternalAllergy

    given("an AllergyMapper, Allergy, and ExternalAllergy instances") {

        beforeEachTest {
            allergyMapper = AllergyMapper()

            sampleAllergy = Allergy(0L,
                    "Sample allergy title",
                    4L,
                    "Sample allergy details")

            sampleExternalAllergy = ExternalAllergy(
                    sampleAllergy.id,
                    sampleAllergy.name,
                    sampleAllergy.severity,
                    sampleAllergy.details,
                    patientTestHash)
        }

        on("transforming from internal to external allergy") {
            val transformedAllergy = allergyMapper.transform(sampleAllergy, patientTestHash)

            it ("it adds patient hash to external allergy instance and keeps all other fields") {
                assert(transformedAllergy == sampleExternalAllergy)
            }
        }

        on("transforming from external to internal allergy") {
            val transformedAllergy = allergyMapper.transform(sampleExternalAllergy)

            it ("it removes patient hash from internal allergy instance and keeps all other fields") {
                assert(transformedAllergy == sampleAllergy)
            }
        }

    }
})