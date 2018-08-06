package com.example.multi_compass

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import android.content.Context.VIBRATOR_SERVICE
import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.os.Vibrator


class CanvasDraw(context: Context, attr: AttributeSet?) : View(context, attr) {

    private val paint: Paint = Paint()
    private var positionX: Float = 0f
    private var positionY: Float = 0f
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var height: Float = 0f
    private var direction: Float = 0f
    private val scale: Float = 20f
    private var radius: Float = 0f
    private var lpfX: LowPassFilter = LowPassFilter()
    private var lpfY: LowPassFilter = LowPassFilter()
    private var lpfD: LowPassFilter = LowPassFilterHandleRotation()
    private var firstSettingFlag = true
    private var vibFlag = true

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if(firstSettingFlag) {
            centerX = canvas.width.toFloat() / 2f
            centerY = canvas.height.toFloat() / 2f
            height = canvas.height.toFloat()
            radius =centerX*0.8f
        }
        paint.isAntiAlias = true
        paint.color = Color.argb(255, 0, 0, 0)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(-radius*sin(-direction)+centerX,-radius*cos(-direction)+centerY,20f,paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        canvas.drawCircle(centerX, centerY, radius, paint)
        canvas.drawCircle(positionX, positionY,100f, paint)
    }

    fun setPosition(xp: Float, yp: Float) {
        positionX = lpfX.get((-1f)*xp/scale*height+centerX)
        positionY = lpfY.get(yp/scale*height+centerY)
        val range = 10f
        if(positionX <= centerX+range && positionX>= centerX-range && positionY <= centerY+range && positionY >= centerY-range && vibFlag) {
            val vibrator: Vibrator = context.getSystemService(VIBRATOR_SERVICE) as Vibrator
            val vibrationEffect: VibrationEffect = VibrationEffect.createOneShot(20, DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
            vibFlag = false
        }
        else {
            vibFlag = true
        }
        // 再描画
        invalidate()
    }

    fun setDirection(dr: Float) {
        direction = lpfD.get(dr)
        invalidate()
    }
}

open class LowPassFilter {
    protected var beforeParam: Float? = null

    open fun get(rowParam: Float): Float {
        val bp =beforeParam
        if(bp != null) {
            beforeParam = rate(bp, rowParam)
        }
        else {
            beforeParam = rowParam
        }
        return beforeParam ?: rowParam
    }

    protected fun rate(old: Float, new:Float): Float {
        return old*0.9f+new*0.1f
    }
}

class LowPassFilterHandleRotation: LowPassFilter() {
    override fun get(rowParam: Float): Float {
        val bp = beforeParam
        if(bp != null) {
            if(abs(rowParam-bp)>abs(rowParam-bp-2*PI)) {
                beforeParam = rate(bp+(2*PI).toFloat(), rowParam)
            }
            else if (abs(rowParam-bp)>abs(rowParam-bp+2*PI)) {
                beforeParam = rate(bp-(2*PI).toFloat(), rowParam)
            }
            else {
                beforeParam = rate(bp, rowParam)
            }
        }
        else {
            beforeParam = rowParam
        }
        return beforeParam ?: rowParam
    }
}