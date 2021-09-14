package com.meetingdoctors.chat.views.nps.view

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.meetingdoctors.chat.R
import com.meetingdoctors.chat.views.nps.NO_RATING_SELECTED


internal class IndicatorRatingBar : LinearLayout {

    private var isBarRated = false
    private var currentRateSelected = NO_RATING_VALUE

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        setupAttributes(attrs)
    }

    fun isBarRated(): Boolean {
        return isBarRated
    }

    private fun setupView(type: RateType) {

        val view = when (type) {
            RateType.NUMERIC -> inflate(context, R.layout.layout_indicator_numeric_rating_bar, null)
            RateType.STAR -> inflate(context, R.layout.layout_indicator_star_rating_bar, null)
        }

        orientation = HORIZONTAL
        addView(view)
        setViewActions()
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.IndicatorRatingBar, 0, 0)

        try {
            when (typedArray.getInteger(R.styleable.IndicatorRatingBar_rateType, 0)) {
                NUMERIC_TYPE -> setupView(RateType.NUMERIC)
                STAR_TYPE -> setupView(RateType.STAR)
                else -> setupView(RateType.NUMERIC)
            }

        } catch (exception: Exception) {
            Log.e("IndicatorRatingBar", "Exception: ${exception.localizedMessage}")
        } finally {
            typedArray.recycle()
        }
    }

    private fun setViewActions() {
        val itemsBarIds = resources.getStringArray(R.array.indicator_rating_bar_item_ids)
        val itemsBarColors = resources.getStringArray(R.array.indicator_rating_bar_item_colors)

        for (itemBar in itemsBarIds) {
            val viewId = this.resources.getIdentifier(itemBar, "id", context.packageName)
            val item = findViewById<TextView>(viewId)

            item.setOnClickListener {
                val position = (it as TextView).text.toString()
                currentRateSelected = position.toInt()
                drawItemPositions(position.toInt(), itemsBarIds, itemsBarColors[position.toInt()])
                isBarRated = true
            }
        }
    }

    private fun drawItemPositions(positionClicked: Int, itemsIds: Array<String>, colorBar: String) {

        val colorSelected = getBarColor(colorBar)

        for (itemPos in 0..INDICATOR_RATE_BAR_SIZE) {
            val viewId = this.resources.getIdentifier(itemsIds[itemPos], "id", context.packageName)
            val item = findViewById<TextView>(viewId)

            when (itemPos <= positionClicked) {
                true -> {
                    item.setBackgroundColor(resources.getColor(colorSelected))
                    item.setTextColor(resources.getColor(R.color.meetingdoctors_white))

                    when (itemPos) {
                        STARTING_ITEM -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val shape = GradientDrawable()
                                shape.shape = GradientDrawable.RECTANGLE

                                shape.setColor(context.getColor(colorSelected))
                                shape.cornerRadii = floatArrayOf(100f, 100f, 0f, 0f, 0f, 0f, 100f, 100f)

                                item.setBackgroundDrawable(shape)
                            }  else {
                                item.setBackgroundColor(resources.getColor(colorSelected))
                            }
                        }
                        END_ITEM -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val shape = GradientDrawable()
                                shape.shape = GradientDrawable.RECTANGLE

                                shape.setColor(context.getColor(colorSelected))
                                shape.cornerRadii = floatArrayOf(0f, 0f, 100f, 100f, 100f, 100f, 0f, 0f)

                                item.setBackgroundDrawable(shape)
                            }  else {
                                item.setBackgroundColor(resources.getColor(colorSelected))
                            }
                        }
                        else -> item.setBackgroundColor(resources.getColor(colorSelected))
                    }
                }
                false -> {
                    if (itemPos == INDICATOR_RATE_BAR_SIZE) {
                        item.background = resources.getDrawable(R.drawable.nps_dialognumeric_bar_end_item)
                    } else {
                        item.background = resources.getDrawable(R.drawable.nps_dialog_numeric_bar_item)
                    }

                    item.setTextColor(resources.getColor(android.R.color.black))
                }
            }
        }
    }

    private fun getBarColor(colorBar: String): Int {
        return when (colorBar) {
            resources.getString(R.string.nps_dialog_negative_nps_valoration_segment) -> R.color.nps_bar_color_red
            resources.getString(R.string.nps_dialog_moderate_nps_valoration_segment) -> R.color.nps_bar_color_orange
            resources.getString(R.string.nps_dialog_positive_nps_valoration_segment) -> R.color.nps_bar_color_green
            else -> R.color.nps_bar_color_red
        }
    }

    fun getCurrentRateSelected(): String {
        return currentRateSelected.toString()
    }
}

internal enum class RateType {
    NUMERIC, STAR
}

private const val NUMERIC_TYPE = 0
private const val STAR_TYPE = 1
private const val STARTING_ITEM = 0
private const val END_ITEM = 10
private const val INDICATOR_RATE_BAR_SIZE = 10
private const val NO_RATING_VALUE = -1
