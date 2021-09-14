package com.meetingdoctors.chat.activities.medicalhistory

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.TitleBarBaseActivity
import com.meetingdoctors.chat.data.Repository.Companion.instance
import com.meetingdoctors.chat.domain.entities.Medication
import com.meetingdoctors.chat.views.MedicalHistoryItemTitleBar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.mediquo_activity_medication.*
import kotlinx.android.synthetic.main.mediquo_layout_medical_history_item_title_bar.*

/**
 * Created by Héctor Manrique on 4/15/21.
 */

class MedicationDetailActivity : TitleBarBaseActivity() {
    var medication: Medication? = null

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleBar(MedicalHistoryItemTitleBar(this, getString(R.string.meetingdoctors_medical_history_medication_title)))
        setContentView(R.layout.mediquo_activity_medication)
        val medicationId = intent.getLongExtra("medication_id", 0)
        if (medicationId != 0L) {
            instance?.getMedicalHistoryRepository()
                    ?.getMedicationById(medicationId)
                    ?.subscribe({ medication ->
                        this@MedicationDetailActivity.medication = medication
                        setViewMode()
                        name?.setText(medication.name)
                        posology!!.setText(medication.posology)
                        details!!.setText(medication.details)
                    }) {
                        finish()
                    }
        } else {
            setEditMode()
        }
        edit_button?.setOnClickListener { setEditMode() }
        name?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!TextUtils.isEmpty(name?.text)) {
                    name_input?.isErrorEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
        posology?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!TextUtils.isEmpty(posology!!.text)) {
                    posology_input?.isErrorEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
        save_button?.setOnClickListener(View.OnClickListener {
            if (!allFieldsValid()) {
                return@OnClickListener
            }
            if (medication != null) {
                medication!!.name = name?.text.toString()
                medication!!.posology = posology!!.text.toString()
                medication!!.details = details!!.text.toString()
                instance?.getMedicalHistoryRepository()
                        ?.putMedication(medication!!)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe({ (id, name, posology1, details1) -> onBackPressed() }) { throwable ->
                            Log.e("putMedication", "Throwable exception occurred: " + throwable.localizedMessage)
                            onBackPressed()
                        }
            } else {
                val medication = Medication(0L,
                        name?.text.toString(),
                        posology?.text.toString(),
                        details?.text.toString())
                instance?.getMedicalHistoryRepository()
                        ?.postMedication(medication)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe({ (id, name, posology1, details1) -> onBackPressed() }) { throwable ->
                            Log.e("postMedication", "Throwable exception occurred: " + throwable.localizedMessage)
                            onBackPressed()
                        }
            }
        })
        delete_button?.setOnClickListener { showConfirmDeletionDialog() }
    }

    private fun allFieldsValid(): Boolean {
        if (TextUtils.isEmpty(name?.text)) {
            name_input?.isErrorEnabled = true
            name_input?.error = getString(R.string.meetingdoctors_medical_history_required)
        } else {
            name_input?.isErrorEnabled = false
        }
        if (TextUtils.isEmpty(posology?.text)) {
            posology_input?.isErrorEnabled = true
            posology_input?.error = getString(R.string.meetingdoctors_medical_history_required)
        } else {
            posology_input?.isErrorEnabled = false
        }
        val detailsLength = details?.text?.length ?: 0
        return !(name_input?.isErrorEnabled == true
                || posology_input?.isErrorEnabled == true
                || detailsLength > 255)
    }

    private fun setEditMode() {
        edit_button?.visibility = View.GONE
        save_button?.visibility = View.VISIBLE
        delete_button?.visibility = if (medication != null) View.VISIBLE else View.GONE
        name?.apply {
            isClickable = true
            isCursorVisible = true
            isFocusable = true
            isFocusableInTouchMode = true
        }
        posology?.apply {
            isClickable = true
            isCursorVisible = true
            isFocusable = true
            isFocusableInTouchMode = true
        }
        details?.apply {
            isClickable = true
            isCursorVisible = true
            isFocusable = true
            isFocusableInTouchMode = true
        }
        name?.requestFocus()
    }

    private fun setViewMode() {
        edit_button?.visibility = View.VISIBLE
        save_button?.visibility = View.GONE
        delete_button?.visibility = View.GONE
        name?.apply {
            isClickable = false
            isCursorVisible = false
            isFocusable = false
            isFocusableInTouchMode = false
        }
        posology?.apply {
            isClickable = false
            isCursorVisible = false
            isFocusable = false
            isFocusableInTouchMode = false
        }
        details?.apply {
            isClickable = false
            isCursorVisible = false
            isFocusable = false
            isFocusableInTouchMode = false
        }
    }

    @SuppressLint("CheckResult")
    private fun showConfirmDeletionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setIcon(android.R.drawable.ic_dialog_alert)
            setMessage(R.string.meetingdoctors_medical_history_delete_confirmation)
            setNegativeButton(R.string.meetingdoctors_medical_history_cancel) { dialog, which ->
                dialog.dismiss()
            }
            setPositiveButton(R.string.meetingdoctors_medical_history_delete) { dialog, which ->
                instance?.getMedicalHistoryRepository()
                        ?.deleteMedication(medication!!.id)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe({ onBackPressed() }) { throwable -> // toast
                            Log.e("deleteMedication", "Throwable exception occurred: " + throwable.localizedMessage)
                            onBackPressed()
                        }
            }
        }
        builder.create().show()
    }
}
