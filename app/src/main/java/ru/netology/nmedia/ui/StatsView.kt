package ru.netology.nmedia.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.nmedia.R
import ru.netology.nmedia.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)

    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()
    private var animationType = 0

    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()
            animationType = getInteger(R.styleable.StatsView_animationType,0)
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        var startFrom = if (animationType == 3) -45F else -90F

        for ((index, datum) in data.withIndex()) {
            val angle = 360F * datum
            paint.color = colors.getOrNull(index) ?: randomColor()
            when (animationType){
                0-> noneAnimation(canvas,angle,index,paint,startFrom)
                1-> parallelAnimation(canvas,angle,index,paint,startFrom)
                2-> sequentialAnimation(canvas,angle,index,paint,startFrom)
                3-> biParallelAnimation(canvas,angle,index,paint,startFrom)
            }
            startFrom +=angle
        }

        canvas.drawText(
            "%.2f%%".format( if (animationType == 0) 100F else progress * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )
    }

    private fun update() {
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        progress = 0F

        valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            duration = 4000
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }

    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())

    private fun biParallelAnimation(canvas: Canvas, angle: Float, index: Int, paint: Paint, startFrom: Float){
        canvas.drawArc(oval, startFrom, (angle * progress)/2, false, paint)
        canvas.drawArc(oval, startFrom , (angle * progress)/-2, false, paint)
    }

    private fun parallelAnimation(canvas: Canvas, angle: Float, index: Int, paint: Paint, startFrom: Float){
        canvas.drawArc(oval, startFrom + 360 * progress, angle * progress, false, paint)
    }

    private fun noneAnimation(canvas: Canvas, angle: Float, index: Int, paint: Paint, startFrom: Float){
        canvas.drawArc(oval, startFrom, angle, false, paint)
    }

    private fun sequentialAnimation(canvas: Canvas, angle: Float, index: Int, paint: Paint, startFrom: Float){

        val drawAngle = angle * (progress - index * 0.25F) * 4
            val endTo: Float

        when (index) {
            0 -> {
                endTo = if (progress < 0.25) drawAngle else angle
                canvas.drawArc(oval, startFrom, endTo, false, paint)
            }
            1 -> {
                if (progress > 0.25){
                    endTo = if (progress < 0.5)  drawAngle else angle
                    canvas.drawArc(oval, startFrom, endTo, false, paint)
                }
            }
            2 -> {
                if (progress > 0.5){
                    endTo = if (progress < 0.75) drawAngle else angle
                    canvas.drawArc(oval, startFrom, endTo, false, paint)
                }
            }
            3 -> {
                if (progress > 0.75){
                    endTo = if (progress < 1) drawAngle else angle
                    canvas.drawArc(oval, startFrom, endTo, false, paint)
                }
            }
        }

    }
}