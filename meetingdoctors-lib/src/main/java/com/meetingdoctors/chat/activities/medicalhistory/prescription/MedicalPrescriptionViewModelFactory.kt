package com.meetingdoctors.chat.activities.medicalhistory.prescription

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.meetingdoctors.chat.ResourceProvider
import com.meetingdoctors.chat.data.Repository
import java.lang.IllegalArgumentException

class MedicalPrescriptionViewModelFactory constructor(val repository: Repository, val context: Context) :
        ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicalPrescriptionViewModel::class.java)) {
            return MedicalPrescriptionViewModel(
                    repository.getPrescriptionUseCase(),
                    repository.getDownloadFileUseCase(),
                    ResourceProvider(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}