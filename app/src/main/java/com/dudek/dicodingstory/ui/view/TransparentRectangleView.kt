package com.dudek.dicodingstory.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.dudek.dicodingstory.R

class TransparentRectangleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.grey_blue)
        style = Paint.Style.FILL
    }

    private val rectangle: RectF = RectF()
    private val cornerRadius = 10f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rectangle.set(
            0f, 0f, width.toFloat(), height.toFloat()
        )
        canvas.drawRoundRect(rectangle, cornerRadius, cornerRadius, paint)
    }

}