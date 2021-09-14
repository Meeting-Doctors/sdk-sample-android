package com.meetingdoctors.chat.adapters

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Spannable
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.BaseActivity
import com.meetingdoctors.chat.data.Repository
import com.meetingdoctors.chat.data.Speciality.Companion.icon
import com.meetingdoctors.chat.domain.DOCTOR_STATUS_OFFLINE
import com.meetingdoctors.chat.domain.DOCTOR_STATUS_ONLINE
import com.meetingdoctors.chat.domain.entitesextensions.getLastMessageTime
import com.meetingdoctors.chat.domain.entitesextensions.getPendingMessageCount
import com.meetingdoctors.chat.domain.entitesextensions.getTitle
import com.meetingdoctors.chat.domain.entitesextensions.getTitleHtml
import com.meetingdoctors.chat.domain.entities.Doctor
import com.meetingdoctors.chat.helpers.SystemHelper.Companion.dpToPixel
import com.meetingdoctors.chat.presentation.entitiesextensions.getCurrentDayOfWeekIndex
import com.meetingdoctors.chat.presentation.entitiesextensions.getNextSchedule
import com.meetingdoctors.chat.presentation.entitiesextensions.getTextSchedule
import com.meetingdoctors.chat.presentation.entitiesextensions.getTimeZoneOffsetInMinutes
import com.meetingdoctors.chat.views.TextDrawable
import com.meetingdoctors.chat.views.configurationmodel.ProfessionalConfigListModel
import kotlinx.android.synthetic.main.mediquo_item_chat_doctor.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

/**
 * Created by Héctor Manrique on 4/12/21.
 */
//TODO: Se han hecho bastantes cambios, se ha elimiando la clase viewholder la cual no era necesaria
class DoctorChatAdapter(context: Activity) : BaseAdapter() {

    companion object {
        private const val VIEW_TYPE_DOCTOR = 0
        private const val VIEW_TYPE_SEPARATOR = 1
        private fun makeImageSpan(context: Context, text: String, color: Int): ImageSpan {
            val customText = " $text "
            val drawable: Drawable = TextDrawable(context, customText, color)
            drawable.mutate()
            // drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            val bottom = dpToPixel(context, 2)
            val right = dpToPixel(context, (customText.length.toDouble() * 5.5).toInt())
            drawable.setBounds(0, 0, right, bottom)
            return ImageSpan(drawable, ImageSpan.ALIGN_BASELINE)
        }
    }

    private val context: Activity = context
    private var doctors: ArrayList<Doctor?>? = null
    private var socketConnected = false
    private var dividerView: ViewGroup? = null
    private var disabledProfessionalColor: Int? = null
    private var beforeDividerSpecialityText: String? = null
    private var dividerIndex = -1
    private var professionalConfigListModel: ProfessionalConfigListModel? = null

    init {
        setConfigModel(ProfessionalConfigListModel(true))
    }

    override fun getCount(): Int {
        return if (doctors != null) doctors!!.size else 0
    }

    override fun getItem(position: Int): Doctor? {
        return if (doctors != null) doctors!![position] else null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == dividerIndex) VIEW_TYPE_SEPARATOR else VIEW_TYPE_DOCTOR
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View? {
        return if (getItemViewType(position) == VIEW_TYPE_SEPARATOR) {
            getViewSeparator(position, view, parent)
        } else {
            getViewDoctor(position, view, parent)
        }
    }

    private fun getViewDoctor(position: Int, view: View?, parent: ViewGroup): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.mediquo_item_chat_doctor, parent, false)
        val dataUpdater = DataUpdater(view.doctor_next_scheduling,
                view.doctor_last_message,
                professionalConfigListModel!!.showSchedule!!)
        view?.doctor_next_scheduling?.post(dataUpdater)


        val doctor = doctors?.get(position)
        if (doctor != null && Repository.instance?.hasAccessToProfessional(doctor) == true) {
            view?.disabled?.visibility = View.GONE
        } else {
            if (disabledProfessionalColor != null) {
                view?.disabled?.setBackgroundColor(disabledProfessionalColor!!)
            }
            view?.disabled?.visibility = View.VISIBLE
        }
        if (socketConnected) {
            when (doctor!!.status) {
                DOCTOR_STATUS_OFFLINE -> view.doctor_status?.setBackgroundResource(R.drawable.mediquo_circle_shadow_red)
                DOCTOR_STATUS_ONLINE -> view.doctor_status?.setBackgroundResource(R.drawable.mediquo_circle_shadow_green)
            }
        } else {
            view.doctor_status?.setBackgroundResource(R.drawable.mediquo_circle_shadow_gray)
        }
        if (doctor?.avatar != null && doctor.avatar.isNotEmpty()) {
            if (view?.doctor_photo != null) {
                Glide.with(context).load(doctor.avatar).apply(RequestOptions.circleCropTransform()).into(view.doctor_photo)
                view.doctor_photo?.visibility = View.VISIBLE
            } else {
                view?.doctor_photo?.visibility = View.INVISIBLE
            }
        } else {
            view?.doctor_photo?.visibility = View.INVISIBLE
        }
        view?.doctor_photo?.setOnClickListener { view1: View? -> BaseActivity.launchDoctorProfile(context, doctor?.hash) }
        view.service_name?.text = doctor?.name
        view?.doctor_speciality_icon?.setImageResource(icon(doctor!!.speciality!!.id!!))
        val title = doctor?.getTitle(context)
        val titleHtml = doctor?.getTitleHtml(context)
        if (position < dividerIndex && beforeDividerSpecialityText != null &&
                beforeDividerSpecialityText?.trim { it <= ' ' }?.isNotEmpty() == true) {
            val spaceForFreeImage = "_ "
            val htmlMessage = HtmlCompat.fromHtml(spaceForFreeImage + titleHtml,
                    HtmlCompat.FROM_HTML_MODE_LEGACY)
            val spannable = Spannable.Factory.getInstance()
                    .newSpannable(if (titleHtml != null) htmlMessage else spaceForFreeImage + title)
            val freeImageSpan = makeImageSpan(context, beforeDividerSpecialityText!!.trim { it <= ' ' },
                    ContextCompat.getColor(context, R.color.meetingdoctors_speciality_color))
            spannable.setSpan(freeImageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            view?.doctor_speciality?.text = spannable
        } else {

            view?.doctor_speciality?.text = if (titleHtml != null) {
                HtmlCompat.fromHtml(titleHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } else {
                title
            }
        }
        dataUpdater.doctor = doctor
        if (doctor!!.getPendingMessageCount() > 0) {
            view?.doctor_pending_messages?.text = doctor?.getPendingMessageCount().toString()
            view?.doctor_pending_messages?.visibility = View.VISIBLE
        } else {
            view?.doctor_pending_messages?.visibility = View.GONE
        }
        view?.divider?.visibility = if (position == count - 1) View.GONE else View.VISIBLE

        // default hidden fields
        if (view?.doctor_overview != null && view.doctor_overview?.visibility != View.GONE) {
            view.doctor_overview?.text = doctor.overview
        }
        if (view?.doctor_schedulings != null && view.doctor_schedulings?.visibility != View.GONE) {
            view.doctor_schedulings?.text = doctor.getTextSchedule(context, getTimeZoneOffsetInMinutes())
        }
        return view
    }

    private fun getViewSeparator(position: Int, view: View?, parent: ViewGroup): View? {
        return dividerView
    }

    fun setConfigModel(configModel: ProfessionalConfigListModel?) {
        professionalConfigListModel = configModel
    }

    inner class DataUpdater(var schedule: TextView,
                            var lastInteraction: TextView,
                            var showSchedule: Boolean) : Runnable {
        var doctor: Doctor? = null
            set(value) {
                field = value
                setText()
            }

        override fun run() {
            setText()
            schedule.postDelayed(this, 1000)
        }

        fun setText() {
            doctor?.let {
                val nextSchedule = it.getNextSchedule(context,
                        "",
                        0,
                        0,
                        getCurrentDayOfWeekIndex(-getTimeZoneOffsetInMinutes() /* to gmt+0*/
                                + it.timezone_offset /* to his gmt */) /* from 1 to 7 */)
                if (nextSchedule != null) {
                    schedule.text = nextSchedule
                } else {
                    schedule.setText(R.string.meetingdoctors_doctor_schedule_not_available)
                }
                if (showSchedule) {
                    schedule.visibility = View.VISIBLE
                } else {
                    schedule.visibility = View.INVISIBLE
                }
                val lastMessageTime = it.getLastMessageTime()
                if (lastMessageTime != null) {
                    lastInteraction.text = PrettyTime().format(lastMessageTime)
                } else {
                    lastInteraction.text = ""
                }
            }
        }

    }

    fun setDividerView(dividerView: ViewGroup?) {
        this.dividerView = dividerView
    }

    fun setDisabledProfessionalColor(disabledProfessionalColor: Int) {
        this.disabledProfessionalColor = disabledProfessionalColor
    }

    fun setBeforeDividerSpecialityText(beforeDividerSpecialityText: String?) {
        this.beforeDividerSpecialityText = beforeDividerSpecialityText
    }

    fun setDoctors(doctors: List<Doctor>) {
        dividerIndex = -1
        this.doctors = ArrayList()
        if (dividerView != null) {
            for (i in doctors.indices) {
                if (dividerIndex == -1 && !Repository.instance!!.hasAccessToProfessional(doctors[i])) {
                    dividerIndex = i
                    this.doctors!!.add(null)
                }
                this.doctors!!.add(doctors[i])
            }
        } else {
            this.doctors!!.addAll(doctors)
        }
        notifyDataSetChanged()
    }

    fun clearDoctors() {
        dividerIndex = -1
        doctors = ArrayList()
        doctors!!.addAll(doctors!!)
        notifyDataSetChanged()
    }

    fun setSocketConnected(socketConnected: Boolean) {
        this.socketConnected = socketConnected
        notifyDataSetChanged()
    }

}
