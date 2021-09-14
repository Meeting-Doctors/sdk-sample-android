package com.meetingdoctors.chat.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import com.meetingdoctors.chat.helpers.SystemHelper.Companion.dpToPixel


/**
 * Created by Héctor Manrique on 4/9/21.
 */

class TextDrawable(context: Context, text: String, colorId: Int) : Drawable() {

    private val context: Context = context
    private val text: String = text
    private val paintBackground: Paint = Paint()

    override fun draw(canvas: Canvas) {
        val fm = Paint.FontMetrics()
        paintBackground.getFontMetrics(fm)
        val rectF = RectF(0f, fm.top, paintBackground.measureText(text), fm.bottom)
        val border = dpToPixel(context, 2)
        canvas.drawRoundRect(rectF, border.toFloat(), border.toFloat(), paintBackground)
        val paintText = Paint()
        paintText.color = Color.WHITE
        paintText.textSize = dpToPixel(context, 11).toFloat()
        paintText.isAntiAlias = true
        paintText.isFakeBoldText = true
        paintText.style = Paint.Style.FILL
        paintText.textAlign = Paint.Align.LEFT
        canvas.drawText(text, 0f, 0f, paintText)
    }

    override fun setAlpha(alpha: Int) {
        paintBackground.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        paintBackground.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return paintBackground.alpha
    }

    init {
        paintBackground.color = colorId
        paintBackground.textSize = dpToPixel(context, 11).toFloat()
        paintBackground.isAntiAlias = true
        paintBackground.isFakeBoldText = true
        paintBackground.style = Paint.Style.FILL
        paintBackground.textAlign = Paint.Align.LEFT
    }
}
