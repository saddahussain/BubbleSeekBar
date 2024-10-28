package com.saddahussain.bubbleseekbar

import android.content.*
import android.graphics.*
import android.util.*
import android.view.*
import android.widget.*
import kotlin.jvm.functions.Function1
import kotlin.let
import kotlin.ranges.coerceIn
import kotlin.text.trim

class BubbleSeekBar(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var thumbRadius = dpToPx(20f) // Default thumb radius
    private var thumbX = thumbRadius
    private var paint = Paint()
    private var path = Path()
    private var progress = 0f

    private var minProgress = 0f
    private var maxProgress = 100f

    private var curveHeight = dpToPx(19f) // Adjust this to make the curve a bit smoother
    private var thumbOffset = dpToPx(8f) // Adjust this to move the thumb up
    private var thumbPadding = dpToPx(24f) // Adjust this to add space on left and right sides of thumb
    private var textView: TextView? = null
    private var suffix = ""

    // Color properties for customization
    private var lineColor = Color.BLACK
    private var thumbColor = Color.BLACK
    private var textColor = Color.WHITE

    // Add a listener for progress changes
    private var onProgressChangeListener: ((Float) -> Unit)? = null
    // Add a variable to store the initial progress when touch starts
    private var initialProgress = 0f

    // Add flag to control touch events
    private var isTouchEnabled = true

    init {
        paint.isAntiAlias = true
        paint.strokeWidth = dpToPx(1f)
        paint.style = Paint.Style.STROKE
        paint.color = lineColor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val midY = height / 2f

        // Set paint color for line
        paint.color = lineColor

        // Draw the straight line to the left of the thumb
        canvas.drawLine(0f, midY, thumbX - thumbRadius - thumbPadding, midY, paint)

        // Draw the rounded curve above the thumb using a cubic Bezier curve
        path.reset()
        path.moveTo(thumbX - thumbRadius - thumbPadding, midY)
        path.cubicTo(thumbX - thumbPadding, midY, thumbX - thumbPadding, midY - curveHeight, thumbX, midY - curveHeight)
        path.cubicTo(thumbX + thumbPadding, midY - curveHeight, thumbX + thumbPadding, midY, thumbX + thumbRadius + thumbPadding, midY)
        canvas.drawPath(path, paint)

        // Draw the straight line to the right of the thumb
        canvas.drawLine(thumbX + thumbRadius + thumbPadding, midY, width.toFloat(), midY, paint)

        // Set paint color for thumb
        paint.style = Paint.Style.FILL
        paint.color = thumbColor
        canvas.drawCircle(thumbX, midY + thumbOffset, thumbRadius, paint)
        paint.style = Paint.Style.STROKE
        paint.color = lineColor // Reset to line color for next operations

        // Update the position of the TextView
        textView?.let {
            val textX = (thumbX - it.width / 2).coerceIn(0f, width - it.width.toFloat())
            val textY = (midY - curveHeight - it.height + dpToPx(10f)).coerceIn(0f, height - it.height.toFloat())
            it.x = textX
            it.y = textY
            it.text = (progress.toInt().toString() + " " + suffix).trim()
            it.setTextColor(textColor)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Check if touch events are enabled
        if (!isTouchEnabled) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialProgress = progress
                updateProgressFromTouch(event.x)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                updateProgressFromTouch(event.x)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                updateProgressFromTouch(event.x)
                // Notify the listener only if the progress has changed since touch started
                if (progress != initialProgress) {
                    onProgressChangeListener?.invoke(progress)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateProgressFromTouch(touchX: Float) {
        thumbX = touchX.coerceIn(thumbRadius + thumbPadding, width - thumbRadius - thumbPadding)
        progress = ((thumbX - thumbRadius - thumbPadding) / (width - 2 * (thumbRadius + thumbPadding))) * (maxProgress - minProgress) + minProgress
        invalidate()
    }

    fun getProgress(): Float {
        return progress
    }

    fun setSuffix(it: String) {
        suffix = it
    }

    fun setProgress(value: Float) {
        //val oldProgress = progress
        progress = value.coerceIn(minProgress, maxProgress)
        updateThumbPosition(progress) // Update thumb position based on progress
        // Notify the listener if progress has changed
        /* if (progress != oldProgress) {
             onProgressChangeListener?.invoke(progress)
         }*/
    }

    // Add method to enable/disable touch events
    fun setTouchEnabled(enabled: Boolean) {
        isTouchEnabled = enabled
    }

    // Add method to check if touch is enabled
    fun isTouchEnabled(): Boolean {
        return isTouchEnabled
    }

    fun setMinMaxProgress(min: Float, max: Float) {
        minProgress = min
        maxProgress = max
    }

    fun attachTextView(tv: TextView) {
        textView = tv
    }

    fun setLineColor(color: Int) {
        lineColor = color
        invalidate()
    }

    fun setThumbColor(color: Int) {
        thumbColor = color
        invalidate()
    }

    fun setTextColor(color: Int) {
        textColor = color
        textView?.setTextColor(color)
        invalidate()
    }

    fun setOnProgressChangeListener(listener: (Float) -> Unit) {
        onProgressChangeListener = listener
    }

    fun setThumbRadius(radius: Float) {
        thumbRadius = dpToPx(radius)
        curveHeight = thumbRadius * 0.95f
        thumbOffset = thumbRadius * 0.4f
        thumbPadding = thumbRadius * 1.2f
        invalidate()
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateThumbPosition(progress)
    }

    private fun updateThumbPosition(progress: Float) {
        thumbX = ((progress - minProgress) / (maxProgress - minProgress)) * (width - 2 * (thumbRadius + thumbPadding)) + thumbRadius + thumbPadding
        invalidate()
    }
}
