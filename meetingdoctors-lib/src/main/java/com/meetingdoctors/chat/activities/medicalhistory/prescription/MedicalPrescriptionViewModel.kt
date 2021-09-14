package com.meetingdoctors.chat.activities.medicalhistory.prescription

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.meetingdoctors.chat.Event
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.ResourceProvider
import com.meetingdoctors.chat.data.FileMimeType
import com.meetingdoctors.chat.domain.entities.Prescription
import com.meetingdoctors.chat.domain.usecase.DownloadFileUseCase
import com.meetingdoctors.chat.domain.usecase.GetPrescriptionUseCase

class MedicalPrescriptionViewModel(private val getPrescriptionUseCase: GetPrescriptionUseCase,
                                   private val downloadFileUseCase: DownloadFileUseCase,
                                   private val resourceProvider: ResourceProvider) : ViewModel() {

    private val _medicalPrescriptionOptionsState = MutableLiveData<MedicalPrescriptionOptionsState>()
    val medicalPrescriptionOptionsState: LiveData<MedicalPrescriptionOptionsState>
        get() = _medicalPrescriptionOptionsState

    private val _medicalPrescriptionEvent = MutableLiveData<Event<MedicalPrescriptionEvent>>()
    val medicalPrescriptionEvent: LiveData<Event<MedicalPrescriptionEvent>>
        get() = _medicalPrescriptionEvent

    var prescription: Prescription? = null

    fun init() {

        getMedicalPrescription()
    }

    private fun getMedicalPrescription() {

        getPrescriptionUseCase.execute({ prescription ->

            this.prescription = prescription
            _medicalPrescriptionOptionsState.postValue(MedicalPrescriptionOptionsState.Success(prescription))

        }, { error ->

            _medicalPrescriptionOptionsState.postValue(MedicalPrescriptionOptionsState.Error(error))
        },
                Unit)

    }

    fun onDownloadButtonClick() {

        prescription?.let {

            downloadFileUseCase.execute({

                //Download Success
                _medicalPrescriptionEvent.value = Event(MedicalPrescriptionEvent.DownloadPrescriptionSuccess)

            }, {
                _medicalPrescriptionEvent.value = Event(MedicalPrescriptionEvent.DownloadPrescriptionError)

            }, DownloadFileUseCase.Params(
                    it.prescriptionUrl,
                    generatePrescriptionFileName(it.lastModifiedDate, it.lastModifiedHour),
                    FileMimeType.PDF))
        }
    }

    fun onRefresh() {

        getMedicalPrescription()

    }

    private fun generatePrescriptionFileName(date: String, hour: String): String {

        val properFileNameDate = date.replace('/', '-')

        return resourceProvider.getString(R.string.meetingdoctors_medical_history_prescriptions_file_name,
                "$properFileNameDate-$hour")
    }
}