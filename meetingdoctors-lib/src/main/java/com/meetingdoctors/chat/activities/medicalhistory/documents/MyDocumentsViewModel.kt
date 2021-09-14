package com.meetingdoctors.chat.activities.medicalhistory.documents

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.meetingdoctors.chat.activities.medicalhistory.utils.MedicalHistoryVisibilityOptions
import com.meetingdoctors.chat.domain.entities.MedicalHistoryOption
import com.meetingdoctors.chat.domain.usecase.AreReportsOptionEnabledUseCase

class MyDocumentsViewModel(private val areReportsOptionEnabledUseCase: AreReportsOptionEnabledUseCase,
                           private val visibilityOptionsList: HashMap<MedicalHistoryVisibilityOptions, Boolean>)
    : ViewModel() {

    private var reportsEnabled: Boolean = false
    private val medicalHistoryOptions = mutableListOf<MedicalHistoryOption>()
    private val _medicalHistoryOptionsState = MutableLiveData<MedicalHistoryOptionsState>()
    val medicalHistoryOptionsState = _medicalHistoryOptionsState

    fun init() {

        areReportsOptionEnabledUseCase.execute({ reportsEnabled ->

            this.reportsEnabled = reportsEnabled
            generateProperOptionsList()

        }, {
            generateProperOptionsList()

        }, Unit)
    }

    private fun generateProperOptionsList() {

        medicalHistoryOptions.apply {
            if (reportsEnabled) {
                if (visibilityOptionsList[MedicalHistoryVisibilityOptions.reports] == true) {
                    this.add(MedicalHistoryOption.VIDEOCALL_REPORT)
                }
            }
            if (visibilityOptionsList[MedicalHistoryVisibilityOptions.referrals] == true) {
                this.add(MedicalHistoryOption.DERIVATION)
            }
            if (visibilityOptionsList[MedicalHistoryVisibilityOptions.prescriptions] == true) {
                this.add(MedicalHistoryOption.PRESCRIPTION)
            }
        }

        _medicalHistoryOptionsState.postValue(MedicalHistoryOptionsState.Success(medicalHistoryOptions))

    }

}
