package com.example.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton

class DrawingView(context: Context, attr: AttributeSet):View(context, attr) {
    private var nDrawPath: CustomPath? = null
    private var nCanvasBitmap: Bitmap? = null
    private var nCanvasPaint: Paint? = null
    private var nDrawPaint: Paint? = null
    private var color = Color.BLACK
    private var brushsize: Float = 0.toFloat()
    private var canvas: Canvas? = null
    private var nPaths = ArrayList<CustomPath>()
    private var undoPaths = ArrayList<CustomPath>()

    init {
        setUpdrawing()
    }

    fun undomove(){
    if(nPaths.size > 0){
        undoPaths.add(nPaths.removeAt(nPaths.size - 1))
    }
        invalidate()
    }

    private fun setUpdrawing(){
        nDrawPaint = Paint()
        nDrawPath = CustomPath(color, brushsize)
        nDrawPaint!!.color = color
        nDrawPaint!!.style = Paint.Style.STROKE
        nDrawPaint!!.strokeJoin = Paint.Join.ROUND
        nDrawPaint!!.strokeCap = Paint.Cap.ROUND
        nCanvasPaint = Paint(Paint.DITHER_FLAG)


    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        nCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(nCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {               //draw on the screen
        super.onDraw(canvas)
        canvas.drawBitmap(nCanvasBitmap!!,0f,0f,nCanvasPaint)

        for (path in nPaths){
            nDrawPaint!!.strokeWidth = path.brushthickness
            nDrawPaint!!.color = path.col
            canvas.drawPath(path, nDrawPaint!!)

        }

        if(!nDrawPath!!.isEmpty) {
            nDrawPaint!!.strokeWidth = nDrawPath!!.brushthickness
            nDrawPaint!!.color = nDrawPath!!.col
            canvas.drawPath(nDrawPath!!, nDrawPaint!!)
        }
    }

    fun setSizeForBrush(newsize:Float){
        brushsize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newsize, resources.displayMetrics)
        nDrawPaint!!.strokeWidth = brushsize
    }

    fun setnewColor(necolor: Int){
        color = necolor
        nDrawPaint!!.color = color
    }


    fun setColor(newcolor: String){
        color = Color.parseColor(newcolor)
        nDrawPaint!!.color = color
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchx = event?.x
        val touchy = event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                nDrawPath!!.col = color
                nDrawPath!!.brushthickness = brushsize

                nDrawPath!!.reset()
                nDrawPath!!.moveTo(touchx!!,touchy!!)
            }

            MotionEvent.ACTION_MOVE->{
                nDrawPath!!.lineTo(touchx!!,touchy!!)
            }

            MotionEvent.ACTION_UP->{
                nPaths.add(nDrawPath!!)
                nDrawPath = CustomPath(color,brushsize)
            }
            else-> return false
        }
            invalidate()

            return true
        }

    internal inner class CustomPath(var col:Int, var brushthickness:Float): Path()
    }



