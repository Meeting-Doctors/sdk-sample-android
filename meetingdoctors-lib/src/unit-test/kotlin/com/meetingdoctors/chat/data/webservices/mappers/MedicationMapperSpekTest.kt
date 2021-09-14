package com.meetingdoctors.chat.data.webservices.mappers

import com.meetingdoctors.chat.UnitTestConstants.patientTestHash
import com.meetingdoctors.chat.data.webservices.entities.ExternalMedication
import com.meetingdoctors.chat.domain.entities.Medication
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object MedicationMapperSpekTest : Spek({

    lateinit var medicationMapper: MedicationMapper
    lateinit var sampleMedication: Medication
    lateinit var sampleExternalMedication: ExternalMedication

    given("an MedicationMapper, Medication, and ExternalMedication instances") {

        beforeEachTest {
            medicationMapper = MedicationMapper()

            sampleMedication = Medication(0L,
                    "Sample medication name",
                    "Sample medication posology",
                    "Sample medication details")

            sampleExternalMedication = ExternalMedication(
                    sampleMedication.id,
                    sampleMedication.name,
                    sampleMedication.posology,
                    sampleMedication.details,
                    patientTestHash)
        }

        on("transforming from internal to external medication") {
            val transformedMedication = medicationMapper.transform(sampleMedication, patientTestHash)

            it ("it adds patient hash to external medication instance and keeps all other fields") {
                assert(transformedMedication == sampleExternalMedication)
            }
        }

        on("transforming from external to internal medication") {
            val transformedMedication = medicationMapper.transform(sampleExternalMedication)

            it ("it removes patient hash from internal medication instance and keeps all other fields") {
                assert(transformedMedication == sampleMedication)
            }
        }

    }
})