package com.meetingdoctors.chat.data.webservices.mappers

import com.meetingdoctors.chat.UnitTestConstants.patientTestHash
import com.meetingdoctors.chat.data.webservices.entities.ExternalDisease
import com.meetingdoctors.chat.domain.entities.Disease
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object DiseaseMapperSpekTest : Spek({

    lateinit var diseaseMapper: DiseaseMapper
    lateinit var sampleDisease: Disease
    lateinit var sampleExternalDisease: ExternalDisease

    given("a DiseaseMapper, Disease, and ExternalDisease instances") {

        beforeEachTest {
            diseaseMapper = DiseaseMapper()

            sampleDisease = Disease(0L,
                    "Sample disease title",
                    "Sample disease details",
                    "2016-01-01",
                    null)

            sampleExternalDisease = ExternalDisease(
                    sampleDisease.id,
                    sampleDisease.name,
                    sampleDisease.details,
                    sampleDisease.diagnosisDate,
                    sampleDisease.resolutionDate,
                    patientTestHash)
        }

        on("transforming from internal to external disease") {
            val transformedDisease = diseaseMapper.transform(sampleDisease, patientTestHash)

            it ("it adds patient hash to external disease instance and keeps all other fields") {
                assert(transformedDisease == sampleExternalDisease)
            }
        }

        on("transforming from external to internal disease") {
            val transformedDisease = diseaseMapper.transform(sampleExternalDisease)

            it ("it removes patient hash from internal disease instance and keeps all other fields") {
                assert(transformedDisease == sampleDisease)
            }
        }

    }
})