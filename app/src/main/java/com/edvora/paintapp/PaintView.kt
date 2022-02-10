package com.edvora.paintapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import com.edvora.paintapp.MainActivity.Companion.paintBrush
import com.edvora.paintapp.util.ToolsPaint
import kotlin.math.*


class PaintView : View {
    private var params: ViewGroup.LayoutParams? = null
    private var currentX = 0f
    private var currentY = 0f
    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    companion object {
        var ellipsePathList = ArrayList<Path>()
        var ellipseColorList = ArrayList<Int>()
        var rectanglePathList = ArrayList<Path>()
        var rectangleColorList = ArrayList<Int>()
        var arrowPathList = ArrayList<Path>()
        var arrowColorList = ArrayList<Int>()
        var pencilPathList = ArrayList<Path>()
        var pencilColorList = ArrayList<Int>()
        var pencilPath = Path()
        var arrowPath = Path()
        var rectanglePath = Path()
        var ellipsePath = Path()
        var currentBrush = Color.BLACK
        var currentToolPaint: ToolsPaint = ToolsPaint.Pencil
    }

    constructor(context: Context) : this(context, null) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        paintBrush.isAntiAlias = true
        paintBrush.color = currentBrush
        paintBrush.style = Paint.Style.STROKE
        paintBrush.strokeJoin = Paint.Join.ROUND
        paintBrush.strokeWidth = 8f
        params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }


    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
        for (i in arrowPathList.indices) {
            paintBrush.color = arrowColorList[i]
            canvas.drawPath(arrowPathList[i], paintBrush)
        }
        for (i in ellipsePathList.indices) {
            paintBrush.color = ellipseColorList[i]
            canvas.drawPath(ellipsePathList[i], paintBrush)
        }
        for (i in rectanglePathList.indices) {
            paintBrush.color = rectangleColorList[i]
            canvas.drawPath(rectanglePathList[i], paintBrush)
        }
        for (i in pencilPathList.indices) {
            paintBrush.color = pencilColorList[i]
            canvas.drawPath(pencilPathList[i], paintBrush)
        }

        when (currentToolPaint) {

            ToolsPaint.Rectangle -> drawRectangle(canvas)
            ToolsPaint.Ellipse -> drawEllipse(canvas)
            ToolsPaint.Arrow -> drawArrow(
                paintBrush,
                canvas,
                currentX,
                currentY,
                motionTouchEventX,
                motionTouchEventY,
            )
        }
    }

    private fun touchStart() {
//        pencilPath.reset()
//        arrowPath.reset()
//        rectanglePath.reset()
//        ellipsePath.reset()
        pencilPath.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private fun touchMove() {
        val dx = abs(motionTouchEventX - currentX)
        val dy = abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            if (currentToolPaint == ToolsPaint.Pencil) {
                pencilPath.quadTo(
                    currentX,
                    currentY,
                    (motionTouchEventX + currentX) / 2,
                    (motionTouchEventY + currentY) / 2
                )
                currentX = motionTouchEventX
                currentY = motionTouchEventY
                // Draw the path in the extra bitmap to cache it.
                extraCanvas.drawPath(pencilPath, paintBrush.apply { color = currentBrush })
            }
        }
        invalidate()
    }

    private fun touchUp() {
        when (currentToolPaint) {
            ToolsPaint.Pencil -> {
                pencilPathList.add(pencilPath)
                pencilColorList.add(currentBrush)
            }

            ToolsPaint.Arrow -> {
                arrowPathList.add(arrowPath)
                arrowColorList.add(currentBrush)
            }
            ToolsPaint.Rectangle -> {
                rectanglePathList.add(rectanglePath)
                rectangleColorList.add(currentBrush)
            }
            ToolsPaint.Ellipse -> {
                ellipsePathList.add(ellipsePath)
                ellipseColorList.add(currentBrush)

            }
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        if (::extraBitmap.isInitialized) extraBitmap.recycle()

        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
    }

    private fun drawRectangle(canvas: Canvas) {
        rectanglePath = Path()
        rectanglePath.moveTo(currentX, currentY)

        rectanglePath.addRect(
            currentX,
            currentY,
            motionTouchEventX,
            motionTouchEventY,
            Path.Direction.CW
        )
        canvas.drawPath(rectanglePath, paintBrush.apply { color = currentBrush })

    }


    private fun drawEllipse(canvas: Canvas) {
        ellipsePath = Path()
        ellipsePath.moveTo(currentX, currentY)
        ellipsePath.addOval(
            currentX,
            currentY,
            motionTouchEventX,
            motionTouchEventY,
            Path.Direction.CW
        )

        canvas.drawPath(ellipsePath, paintBrush.apply { color = currentBrush })
    }

    private fun drawArrow(
        paint: Paint,
        canvas: Canvas,
        startX: Float,
        startY: Float,
        stopX: Float,
        stopY: Float,
    ) {
        val dx = stopX - startX
        val dy = stopY - startY
        val rad = atan2(dy.toDouble(), dx.toDouble()).toFloat()

        arrowPath = Path()
        arrowPath.moveTo(currentX, currentY)
        arrowPath.lineTo(stopX, stopY)
        arrowPath.lineTo(
            (stopX + Math.cos(rad + Math.PI * 0.75) * 40).toFloat(),
            (stopY + Math.sin(rad + Math.PI * 0.75) * 40).toFloat()
        )
        arrowPath.moveTo(stopX, stopY)
        arrowPath.lineTo(
            (stopX + Math.cos(rad - Math.PI * 0.75) * 40).toFloat(),
            (stopY + Math.sin(rad - Math.PI * 0.75) * 40).toFloat()
        )
        canvas.drawPath(arrowPath, paint.apply { color = currentBrush })
    }

}