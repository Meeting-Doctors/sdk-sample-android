package com.meetingdoctors.chat.activities.medicalhistory

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.TitleBarBaseActivity
import com.meetingdoctors.chat.data.Repository.Companion.instance
import com.meetingdoctors.chat.domain.entities.Allergy
import com.meetingdoctors.chat.views.MedicalHistoryItemTitleBar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.mediquo_activity_alergy.*
import kotlinx.android.synthetic.main.mediquo_layout_medical_history_item_title_bar.*

/**
 * Created by Héctor Manrique on 4/15/21.
 */

class AllergyDetailActivity : TitleBarBaseActivity() {
    var allergy: Allergy? = null

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleBar(MedicalHistoryItemTitleBar(this, getString(R.string.meetingdoctors_medical_history_allergy_title)))
        setContentView(R.layout.mediquo_activity_alergy)
        val allergyId = intent.getLongExtra("allergy_id", 0)
        if (allergyId != 0L) {
            instance!!.getMedicalHistoryRepository().getAllergyById(allergyId)
                    .subscribe({ allergy ->
                        this@AllergyDetailActivity.allergy = allergy
                        setViewMode()
                        name?.setText(allergy.name)
                        setSeverity(allergy.severity)
                        details?.setText(allergy.details)
                    }) { finish() }
        } else {
            setSeverity(3L) // default severity
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
        save_button?.setOnClickListener(View.OnClickListener {
            when {
                TextUtils.isEmpty(name?.text) -> {
                    name_input?.isErrorEnabled = true
                    name_input?.error = getString(R.string.meetingdoctors_medical_history_required)
                    return@OnClickListener
                }
                details?.text != null && details!!.text!!.length > 255 -> {
                    return@OnClickListener
                }
                else -> {
                    name_input?.isEnabled = false
                }
            }
            if (allergy != null) {
                allergy!!.name = name?.text.toString()
                allergy!!.severity = getSeverity()
                allergy!!.details = details!!.text.toString()
                instance?.getMedicalHistoryRepository()
                        ?.putAllergy(allergy!!)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe({ (id, name, severity, details1) -> onBackPressed() }) { throwable -> // toast
                            Log.e("ERROR", "Throwable: " + throwable.localizedMessage)
                            onBackPressed()
                        }
            } else {
                val allergy = Allergy(0L,
                        name?.text.toString(),
                        getSeverity(),
                        details?.text.toString())
                instance?.getMedicalHistoryRepository()
                        ?.postAllergy(allergy)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe({ (id, name, severity, details1) -> onBackPressed() }) { throwable ->
                            Log.e("ERROR", "Throwable: " + throwable.localizedMessage)
                            onBackPressed()
                        }
            }
        })
        delete_button?.setOnClickListener { showConfirmDeletionDialog() }
    }

    private fun setEditMode() {
        edit_button?.visibility = View.GONE
        save_button?.visibility = View.VISIBLE
        delete_button?.visibility = if (allergy != null) View.VISIBLE else View.GONE
        name?.apply {
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
        severity_1?.setOnClickListener(OnServerityClickListener(1L))
        severity_2?.setOnClickListener(OnServerityClickListener(2L))
        severity_3?.setOnClickListener(OnServerityClickListener(3L))
        severity_4?.setOnClickListener(OnServerityClickListener(4L))
        severity_5?.setOnClickListener(OnServerityClickListener(5L))
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
        details?.apply {
            isClickable = false
            isCursorVisible = false
            isFocusable = false
            isFocusableInTouchMode = false
        }

        severity_1?.setOnClickListener(null)
        severity_2?.setOnClickListener(null)
        severity_3?.setOnClickListener(null)
        severity_4?.setOnClickListener(null)
        severity_5?.setOnClickListener(null)
    }

    private inner class OnServerityClickListener(private val severity: Long) : View.OnClickListener {
        override fun onClick(v: View) {
            setSeverity(severity)
        }

    }

    private fun setSeverity(severity: Long?) {
        severity_1?.setImageResource(R.drawable.mediquo_circle_gray)
        severity_1?.tag = 0
        severity_2?.setImageResource(R.drawable.mediquo_circle_gray)
        severity_2?.tag = 0
        severity_3?.setImageResource(R.drawable.mediquo_circle_gray)
        severity_3?.tag = 0
        severity_4?.setImageResource(R.drawable.mediquo_circle_gray)
        severity_4?.tag = 0
        severity_5?.setImageResource(R.drawable.mediquo_circle_gray)
        severity_5?.tag = 0
        when (severity?.toInt() ?: 0) {
            5 -> {
                severity_5?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                severity_5?.tag = 1
                severity_4?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                severity_4?.tag = 1
                severity_3?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                severity_3?.tag = 1
                severity_2?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                severity_2?.tag = 1
                severity_1?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                severity_1?.tag = 1
            }
            4 -> {
                severity_4?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                severity_4?.tag = 1
                severity_3?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                severity_3?.tag = 1
                severity_2?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                severity_2?.tag = 1
                severity_1?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                severity_1?.tag = 1
            }

            3 -> {
                severity_3?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                severity_3?.tag = 1
                severity_2?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                severity_2?.tag = 1
                severity_1?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                severity_1?.tag = 1
            }
            2 -> {
                severity_3?.tag = 1
                severity_2?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                severity_2?.tag = 1
                severity_1?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                severity_1?.tag = 1
            }
            1 -> {
                severity_1?.setImageResource(R.drawable.mediquo_circle_medicalhistory)
                severity_1?.tag = 1
            }
        }
    }

    private fun getSeverity(): Long {
        if (severity_5?.tag != null && severity_5?.tag as Int == 1) return 5L
        if (severity_4?.tag != null && severity_4?.tag as Int == 1) return 4L
        if (severity_3?.tag != null && severity_3?.tag as Int == 1) return 3L
        if (severity_2?.tag != null && severity_2?.tag as Int == 1) return 2L
        return if (severity_1?.tag != null && severity_1?.tag as Int == 1) 1L else 0L
    }

    @SuppressLint("CheckResult")
    private fun showConfirmDeletionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setMessage(R.string.meetingdoctors_medical_history_delete_confirmation)
        builder.setNegativeButton(R.string.meetingdoctors_medical_history_cancel) { dialog, which -> dialog.dismiss() }
        builder.setPositiveButton(R.string.meetingdoctors_medical_history_delete) { dialog, which ->
            instance!!.getMedicalHistoryRepository().deleteAllergy(allergy!!.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ onBackPressed() }) {
                        Toast.makeText(this@AllergyDetailActivity,
                                "Ha ocurrido un error, no se ha podido eliminar el elemento.",
                                Toast.LENGTH_SHORT).show()
                    }
        }
        builder.create().show()
    }
}
