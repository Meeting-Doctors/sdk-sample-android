package com.meetingdoctors.chat.activities.medicalhistory.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.meetingdoctors.chat.activities.medicalhistory.utils.MedicalHistoryVisibilityOptions
import com.meetingdoctors.chat.data.Repository
import java.lang.IllegalArgumentException

class MyDocumentsViewModelFactory constructor(val repository: Repository,
                                              private val visibilityOptionsMap: HashMap<MedicalHistoryVisibilityOptions, Boolean>) :
        ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyDocumentsViewModel::class.java)) {
            return MyDocumentsViewModel(
                    repository.getAreReportsOptionEnabledUseCase(),
            visibilityOptionsMap) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}