package com.meetingdoctors.chat.activities.medicalhistory

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.TitleBarBaseActivity
import com.meetingdoctors.chat.data.Repository.Companion.instance
import com.meetingdoctors.chat.domain.entities.Disease
import com.meetingdoctors.chat.views.MedicalHistoryItemTitleBar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.mediquo_activity_disease.*
import kotlinx.android.synthetic.main.mediquo_layout_medical_history_item_title_bar.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.Throws


/**
 * Created by Héctor Manrique on 4/15/21.
 */

@SuppressLint("CheckResult")
class DiseaseDetailActivity : TitleBarBaseActivity() {
    var disease: Disease? = null
    var deleteButton: AppCompatButton? = null
    var diagnosisDateValue: Calendar? = null
    var resolutionDateValue: Calendar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleBar(MedicalHistoryItemTitleBar(this, getString(R.string.meetingdoctors_medical_history_disease_title)))
        setContentView(R.layout.mediquo_activity_disease)

        val diseaseId = intent.getLongExtra("disease_id", 0)
        if (diseaseId != 0L) {
            instance!!.getMedicalHistoryRepository().getDiseaseById(diseaseId)
                    .subscribe({ disease: Disease ->
                        this@DiseaseDetailActivity.disease = disease
                        setViewMode()
                        name?.setText(disease.name)
                        details?.setText(disease.details)
                        setDiseaseDate(true, diagnosis_date, if (disease.diagnosisDate != null) disease.diagnosisDate else "")
                        setDiseaseDate(false, resolution_date, if (disease.resolutionDate != null) disease.resolutionDate else "")
                    }) { throwable: Throwable? -> finish() }
        } else {
            setEditMode()
        }
        edit_button?.setOnClickListener(View.OnClickListener { v: View? -> setEditMode() })
        name?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!TextUtils.isEmpty(name?.text)) {
                    name_input?.isErrorEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
        diagnosis_date?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!TextUtils.isEmpty(diagnosis_date?.text)) {
                    diagnosis_date_input?.isErrorEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
        save_button?.setOnClickListener(View.OnClickListener { v: View? ->
            if (!allFieldsValid()) {
                return@OnClickListener
            }
            if (disease != null) {
                disease!!.name = name?.text.toString()
                disease!!.details = details.text.toString()
                disease!!.diagnosisDate = diagnosis_date?.tag as String?
                disease!!.resolutionDate = resolution_date?.tag as String?
                instance!!.getMedicalHistoryRepository().putDisease(disease!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ disease: Disease? -> onBackPressed() }) { throwable: Throwable? -> onBackPressed() }
            } else {
                val disease = Disease(
                        0L,
                        name?.text.toString(),
                        details.text.toString(),
                        diagnosis_date?.tag as String?,
                        resolution_date?.tag as String?
                )
                instance?.getMedicalHistoryRepository()
                        ?.postDisease(disease)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe({ disease1: Disease? ->
                            onBackPressed()
                        }) { throwable: Throwable? ->
                            onBackPressed()
                        }
            }
        })
        delete_button?.setOnClickListener(View.OnClickListener { v: View? ->
            showConfirmDeletionDialog()
        })
    }

    private fun showDatePicker(firstDate: Boolean) {
        val calendar = Calendar.getInstance()
        var dateToDisplay = Calendar.getInstance()
        if (firstDate) {
            if (diagnosisDateValue != null) {
                dateToDisplay = diagnosisDateValue
            }
        } else {
            if (resolutionDateValue != null) {
                dateToDisplay = resolutionDateValue
            }
        }
        val dialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { arg0: DatePicker?, year: Int, month: Int, day_of_month: Int ->
            calendar[Calendar.YEAR] = year
            calendar[Calendar.MONTH] = month
            calendar[Calendar.DAY_OF_MONTH] = day_of_month
            val apiFormat = "yyyy-MM-dd"
            val dateFormat = SimpleDateFormat(apiFormat, Locale.getDefault())
            if (firstDate) {
                diagnosisDateValue = calendar
                try {
                    setDiseaseDate(true, diagnosis_date, dateFormat.format(calendar.time))
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            } else {
                if (diagnosisDateValue != null && resolutionDateValue != null && diagnosisDateValue!!.timeInMillis > resolutionDateValue!!.timeInMillis) {
                    diagnosisDateValue = calendar
                    try {
                        setDiseaseDate(true, diagnosis_date, dateFormat.format(calendar.time))
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                }
                resolutionDateValue = calendar
                try {
                    setDiseaseDate(false, resolution_date, dateFormat.format(calendar.time))
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
        }, dateToDisplay[Calendar.YEAR], dateToDisplay[Calendar.MONTH], dateToDisplay[Calendar.DAY_OF_MONTH])
        if (firstDate) {
            calendar.add(Calendar.YEAR, 0)
            var maxDate = Calendar.getInstance().timeInMillis
            if (resolutionDateValue != null) {
                maxDate = resolutionDateValue!!.timeInMillis
            }
            dialog.datePicker.maxDate = maxDate // TODO: used to hide future date,month and year
        } else {
            var minDate = Calendar.getInstance().timeInMillis
            if (diagnosisDateValue != null) {
                minDate = diagnosisDateValue!!.timeInMillis
                dialog.datePicker.minDate = minDate
            }
            val maxDate = Math.max(minDate, Calendar.getInstance().timeInMillis)
            dialog.datePicker.maxDate = maxDate // TODO: used to hide previous date,month and year
            calendar.add(Calendar.YEAR, 0)
        }
        dialog.show()
    }

    private fun allFieldsValid(): Boolean {
        if (TextUtils.isEmpty(name?.text)) {
            name_input?.isErrorEnabled = true
            name_input?.error = getString(R.string.meetingdoctors_medical_history_required)
        } else {
            name_input?.isErrorEnabled = false
        }
        val detailLength = details?.text?.length ?: 0
        return !(name_input?.isErrorEnabled == true || detailLength > 255)
    }

    @Throws(ParseException::class)
    private fun setDiseaseDate(firstDate: Boolean, editText: EditText?, date: String? /*y-d-m*/) {
        if (date!!.split("-".toRegex()).toTypedArray().size != 3) return
        val apiFormat = "yyyy-MM-dd"
        val displayFormat = "dd/MM/yyyy"
        val dateFormatter = SimpleDateFormat(apiFormat, Locale.getDefault())
        val label: String
        val dateToShow: Calendar?
        if (firstDate) {
            label = getString(R.string.meetingdoctors_medical_history_disease_diagnosis_date_prefix)
            diagnosisDateValue = Calendar.getInstance()
            diagnosisDateValue?.time = dateFormatter.parse(date)
            dateToShow = diagnosisDateValue
        } else {
            label = getString(R.string.meetingdoctors_medical_history_disease_resolution_date_prefix)
            resolutionDateValue = Calendar.getInstance()
            resolutionDateValue?.time = dateFormatter.parse(date)
            dateToShow = resolutionDateValue
        }
        val displayFormatter = SimpleDateFormat(displayFormat, Locale.getDefault())
        val displayValue = displayFormatter.format(dateToShow!!.time)
        editText?.setText("$label $displayValue")
        editText?.tag = date
    }

    private fun setEditMode() {
        edit_button?.visibility = View.GONE
        save_button?.visibility = View.VISIBLE
        delete_button?.visibility = if (disease != null) View.VISIBLE else View.GONE
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

        diagnosis_date_button?.setOnClickListener { v: View? -> showDatePicker(true) }
        diagnosis_date?.setOnClickListener { v: View? -> showDatePicker(true) }
        resolution_date_button?.setOnClickListener { v: View? -> showDatePicker(false) }
        resolution_date?.setOnClickListener { v: View? -> showDatePicker(false) }
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
        diagnosis_date_button?.setOnClickListener(null)
        diagnosis_date?.setOnClickListener(null)
        resolution_date_button?.setOnClickListener(null)
        resolution_date?.setOnClickListener(null)
    }

    @SuppressLint("CheckResult")
    private fun showConfirmDeletionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setMessage(R.string.meetingdoctors_medical_history_delete_confirmation)
        builder.setNegativeButton(R.string.meetingdoctors_medical_history_cancel) { dialog, which -> dialog.dismiss() }
        builder.setPositiveButton(R.string.meetingdoctors_medical_history_delete) { dialog, which ->
            instance?.getMedicalHistoryRepository()
                    ?.deleteDisease(disease!!.id)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe({ onBackPressed() }
                    ) { throwable: Throwable? -> onBackPressed() }
        }
        builder.create().show()
    }
}
