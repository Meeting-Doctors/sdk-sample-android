package com.meetingdoctors.chat.activities.medicalhistory.prescription

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Keep
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.meetingdoctors.chat.PermissionManager
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.TitleBarBaseActivity
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.domain.entities.Prescription
import com.meetingdoctors.chat.domain.exceptions.PrescriptionException
import com.meetingdoctors.chat.views.HomeTitleBar
import com.meetingdoctors.chat.views.MedicalHistoryListTitleBar
import kotlinx.android.synthetic.main.mediquo_activity_medical_prescription.*

class MedicalPrescriptionActivity : TitleBarBaseActivity() {

    @Keep
    companion object {

        @JvmStatic
        fun newInstance(context: Context) {
            val intent = Intent(context, MedicalPrescriptionActivity::class.java)
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(R.anim.mediquo_right_side_in, R.anim.mediquo_hold)
            }
        }
    }

    private val medicalPrescriptionViewModelFactory: MedicalPrescriptionViewModelFactory by lazy {
        MedicalPrescriptionViewModelFactory(Repository.instance!!, this.applicationContext)
    }

    private val viewModel: MedicalPrescriptionViewModel by lazy {
        ViewModelProvider(
                this,
                medicalPrescriptionViewModelFactory
        ).get(MedicalPrescriptionViewModel::class.java)
    }

    private val permissionManager: PermissionManager by lazy {
        PermissionManager.newInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val homeTitleBar = MedicalHistoryListTitleBar(this, getString(R.string.meetingdoctors_medical_history_prescriptions))
        homeTitleBar.hideAddButton()
        setTitleBar(homeTitleBar)

        setContentView(R.layout.mediquo_activity_medical_prescription)

        initObservers()

        setUpDrugProviderInfo()

        viewModel.init()
    }

    private fun setUpDrugProviderInfo() {
        val metaDataInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData
        val isDrugProviderMessageVisible = metaDataInfo.getBoolean("medicalhistory.prescription.drugProvider", false)
        medicalPrescriptionDrugProviderGroup.visibility = if (isDrugProviderMessageVisible) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun initObservers() {
        viewModel.medicalPrescriptionOptionsState.observe(this, Observer { state ->
            swipeRefreshLayout?.isRefreshing = false
            progressBar?.visibility = View.GONE
            when (state) {
                is MedicalPrescriptionOptionsState.Success -> {
                    renderSuccessState(state.prescription)
                }
                is MedicalPrescriptionOptionsState.Error -> {
                    renderErrorState(state.throwable)
                }
            }
        })

        viewModel.medicalPrescriptionEvent.observe(this, Observer { event ->
            val errorMessage = when (event.getContentIfNotHandled()) {
                is MedicalPrescriptionEvent.DownloadPrescriptionSuccess -> {

                    getString(R.string.meetingdoctors_medical_history_prescriptions_download_success)

                }
                is MedicalPrescriptionEvent.DownloadPrescriptionError -> {

                    getString(R.string.meetingdoctors_medical_history_prescriptions_download_error)
                }
                null -> null
            }

            errorMessage?.let {
                showErrorMessage(it)
            }

        })
    }

    private fun showErrorMessage(errorMessage: String) {
        Snackbar.make(
                findViewById<View>(android.R.id.content),
                errorMessage,
                Snackbar.LENGTH_LONG)
                .show()
    }

    private fun renderSuccessState(prescription: Prescription) {

        lastModifiedDate?.text = getString(
                R.string.meetingdoctors_medical_history_prescriptions_date,
                prescription.lastModifiedDate,
                prescription.lastModifiedHour)

        medicalPrescriptionInnerContainer?.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(prescription.prescriptionUrl)))
            } catch (exception: ActivityNotFoundException) {
                Toast.makeText(this,
                        getString(R.string.meetingdoctors_no_app_found_open_pdf),
                        Toast.LENGTH_LONG)
                        .show()
            }
        }

        swipeRefreshLayout?.setOnRefreshListener {
            swipeRefreshLayout?.isRefreshing = true
            viewModel.onRefresh()
        }

        downloadButton?.setOnClickListener {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (checkIfNeedToRequestPermissions()) {
                    requestPermissionToStorePrescription()
                } else {
                    viewModel.onDownloadButtonClick()
                }
            } else {

                viewModel.onDownloadButtonClick()
            }
        }

        prescriptionErrorMessage?.visibility = View.GONE
        medicalPrescriptionInfoContainer?.visibility = View.VISIBLE
    }

    private fun requestPermissionToStorePrescription() {
        val listener = permissionManager.setAllPermissionListener(
                this,
                findViewById<View>(android.R.id.content) as ViewGroup,
                getString(R.string.meetingdoctors_edit_permissions_write_external)
        ) {
            viewModel.onDownloadButtonClick()
        }
        permissionManager.setRequestPermissions(
                this,
                arrayListOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                listener
        )
    }

    private fun checkIfNeedToRequestPermissions(): Boolean {

        return permissionManager.checkSomePermissionUngranted(
                this,
                listOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
        )
    }

    private fun renderErrorState(throwable: Throwable) {

        when (throwable) {

            is PrescriptionException.PrescriptionNotFoundException -> {
                prescriptionErrorMessage?.text = getString(R.string.meetingdoctors_medical_history_prescription_empty)
            }
            else -> {
                prescriptionErrorMessage?.text = getString(R.string.meetingdoctors_medical_history_prescription_error)
            }
        }

        prescriptionErrorMessage?.visibility = View.VISIBLE
        medicalPrescriptionInfoContainer?.visibility = View.GONE
    }
}