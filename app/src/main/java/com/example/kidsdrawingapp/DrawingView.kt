package com.example.kidsdrawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    //cor e espessura do traçado
    private var mDrawPath: CustomPath? = null

    //uma instância de Bitmap que traz as configurações de cada pixel do 'traço'
    private var mCanvasBitmap: Bitmap? = null

    //estilo do traço: classe Paint contém as informações de estilo e cor sobre como desenhar geometrias, texto e bitmaps.
    private var mDrawPaint: Paint? = null

    //estilo e configrações de cor sobre como desenhar os bitmaps - Instância de exibição de pintura de tela.
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0.toFloat()

    //Uma variável para manter uma cor do traço.
    private var color = Color.BLACK

    /**
     * Uma variável para canvas que será inicializada posteriormente.
     * A classe Canvas contém as chamadas "draw". Para desenhar algo, você precisa de 4 componentes básicos:
     * Um Bitmap para armazenar os pixels, um Canvas para hospedar
     * as chamadas de desenho (escrevendo no bitmap), uma primitiva de desenho (por exemplo, Rect,
     * Caminho, texto, Bitmap) e uma pintura (para descrever as cores e estilos do
     * desenho)
     */
    private var canvas: Canvas? = null //Especifica um bitmap mutável para a tela desenhar

    //variável para persistir o traçado - arrayList para caminhos
    private val mPaths = ArrayList<CustomPath>()
    private val mUndoPaths = ArrayList<CustomPath>()

    init {
        setupDrawing()
    }

    fun onClickUndo() {
        if(mPaths.size > 0){
            mUndoPaths.add(mPaths.removeAt(mPaths.size - 1))
            invalidate()
        }
    }

    //Inicialização dos atributos da classe
    private fun setupDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG) //pesquisar sobre blitting
//        mBrushSize = 0.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    // Este método é chamado quando um traço é desenhado na tela
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        //persisitindo o traçado
        for (path in mPaths) {
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path, mDrawPaint!!)
        }

        if (!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    // atua como um event listener quando um toque é detectado no dispositivo
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                //persisitindo o traçado
                mPaths.add(mDrawPath!!) //adicionado um mDrawPath à lista
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false
        }
        invalidate()
        return true
    }

    /**
     * Este método é chamado quando o pincel ou a borracha
     * os tamanhos devem ser alterados. Este método define o tamno do pincel/borracha
     * para os novos valores dependendo da seleção do usuário.
     */
    fun setSizeForBrush(newSize: Float) {
        mBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize,
            resources.displayMetrics
        )
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setColor(newColor: String) {
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }

    // An inner class for custom path with two params as color and stroke size
    internal inner class CustomPath(
        var color: Int,
        var brushThickness: Float
    ) : android.graphics.Path() {

    }

}