package com.meetingdoctors.chat.adapters

import android.content.Context
import android.text.Html
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.activities.base.BaseActivity
import com.meetingdoctors.chat.data.Message
import com.meetingdoctors.chat.helpers.SystemHelper.Companion.dpToPixel
import com.meetingdoctors.chat.helpers.getHumanReadableFileSize
import com.meetingdoctors.chat.helpers.openFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Héctor Manrique on 4/12/21.
 */
class MessageAdapter(context: Context, messages: MutableList<Message>)
    : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val context: Context = context
    private val messages: MutableList<Message> = messages

    fun clearMessages() {
        messages.clear()
        notifyDataSetChanged()
    }

    //TODO: else should return valid value, onCreateViewHolder not allow return null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout: Int = when (viewType) {
            Message.TYPE_STRING_MINE -> R.layout.mediquo_item_message_mine
            Message.TYPE_STRING_THEIR -> R.layout.mediquo_item_message_their
            Message.TYPE_DATE -> R.layout.mediquo_item_date
            Message.TYPE_TYPING -> R.layout.mediquo_item_typing
            Message.TYPE_IMAGE_MINE -> R.layout.mediquo_item_image_mine
            Message.TYPE_IMAGE_THEIR -> R.layout.mediquo_item_image_their
            Message.TYPE_FILE_MINE -> R.layout.mediquo_item_message_mine
            Message.TYPE_FILE_THEIR -> R.layout.mediquo_item_message_their
            Message.TYPE_ALERT -> R.layout.mediquo_item_alert
            else ->  R.layout.mediquo_item_alert
        }
        return MessageViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }

    override fun onBindViewHolder(viewHolder: MessageViewHolder, position: Int) {
        val message = messages[position]
        when (message.type) {
            Message.TYPE_STRING_MINE, Message.TYPE_STRING_THEIR -> viewHolder.setString(message.time, message.status, message.string)
            Message.TYPE_IMAGE_MINE, Message.TYPE_IMAGE_THEIR -> viewHolder.setImage(message.time, message.status, message.thumbUrl, message.imageUrl, message.imageWidth, message.imageHeight)
            Message.TYPE_FILE_MINE, Message.TYPE_FILE_THEIR -> viewHolder.setFile(message.time, message.status, message.fileName, message.fileUrl, message.fileSize)
            Message.TYPE_DATE -> viewHolder.setDate(message.time)
            Message.TYPE_ALERT -> viewHolder.setString(null, null, message.string)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        return messages[position].type
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val message: TextView?
        private val image: ImageView?
        private val time: TextView?
        private val status: ImageView?
        private val loading: ProgressBar?

        //TODO: We can't remove findViewById and use proper view access because we don't know which layout is inflated on ViewHolder
        init {
            message = itemView?.findViewById<View>(R.id.message) as? TextView
            image = itemView?.findViewById<View>(R.id.image) as? ImageView
            time = itemView?.findViewById<View>(R.id.message_time) as? TextView
            status = itemView?.findViewById<View>(R.id.message_status) as? ImageView
            loading = itemView?.findViewById<View>(R.id.loading) as? ProgressBar
        }

        fun setString(time: Long?, status: Int?, message: String?) {
            if (null == this.message) return
            this.message.autoLinkMask = Linkify.ALL
            this.message.setTextIsSelectable(true)
            this.itemView.setOnClickListener(null)
            this.message.text = message
            setTime(time)
            setStatus(status)
        }

        fun setImage(time: Long?, status: Int?, thumbPath: String?, imagePath: String?, imageWidth: Long?, imageHeight: Long?) {
            if (image == null || thumbPath == null || imagePath == null || imageWidth == null || imageHeight == null) return
            val previewWidth = dpToPixel(context, 200)
            if (imageWidth > 0 && imageHeight > 0) {
                image.layoutParams.width = previewWidth
                image.layoutParams.height = (previewWidth.toDouble() / imageWidth.toDouble()  * imageHeight.toDouble() ).toInt()
                image.requestLayout()
            }
            if (thumbPath.trim { it <= ' ' }.toLowerCase().startsWith("http")) {
                Glide.with(context).load(thumbPath).apply(RequestOptions.overrideOf(previewWidth, 0)).into(image)
            } else {
                Glide.with(context).load(File(thumbPath)).apply(RequestOptions.overrideOf(previewWidth, 0)).into(image)
            }
            image.setOnClickListener(View.OnClickListener { BaseActivity.launchImageViewer(context, imagePath) })
            setTime(time)
            setStatus(status)
        }

        fun setFile(time: Long?, status: Int?, fileName: String?, fileUrl: String?, fileSize: Long?) {
            if (null == message) return
            setTime(time)
            setStatus(status)
            message.autoLinkMask = 0
            message.setTextIsSelectable(false)
            this.itemView.setOnClickListener(null)
            if (fileUrl != null) {
                val htmlMessage = HtmlCompat.fromHtml("<u>$fileName</u> (${getHumanReadableFileSize(fileSize!!)})",
                        HtmlCompat.FROM_HTML_MODE_LEGACY)
                message.text = htmlMessage
                this.itemView.setOnClickListener { openFile(context, fileUrl, fileName) }
            } else {
                message.text = "$fileName (${getHumanReadableFileSize(fileSize!!)}) "
            }
        }

        fun setDate(time: Long?) {
            message!!.autoLinkMask = 0
            message.text = SimpleDateFormat("d MMMM, yyyy", Locale.getDefault()).format(time)
        }

        private fun setTime(time: Long?) {
            if (this.time != null) {
                if (time != null) {
                    this.time.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(time)
                    this.time.visibility = View.VISIBLE
                } else {
                    this.time.visibility = View.GONE
                }
            }
        }

        private fun setStatus(status: Int?) {
            if (this.status != null) {
                if (status != null) {
                    when (status) {
                        0 -> {
                            if (loading != null) loading.visibility = View.VISIBLE
                            this.status.visibility = View.GONE
                        }
                        1 -> {
                            if (loading != null) loading.visibility = View.GONE
                            this.status.setImageResource(R.drawable.mediquo_check)
                            this.status.visibility = View.VISIBLE
                        }
                        2 -> {
                            if (loading != null) loading.visibility = View.GONE
                            this.status.setImageResource(R.drawable.mediquo_check_double)
                            this.status.visibility = View.VISIBLE
                        }
                        3 -> {
                            if (loading != null) loading.visibility = View.GONE
                            this.status.setImageResource(R.drawable.mediquo_check_double)
                            this.status.visibility = View.VISIBLE
                        }
                    }
                } else {
                    this.status.visibility = View.GONE
                }
            }
        }

    }

}
