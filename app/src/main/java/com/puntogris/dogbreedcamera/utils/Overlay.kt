package com.puntogris.dogbreedcamera.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.MutableLiveData

class Overlay(context: Context, attributeSet: AttributeSet) : View(context, attributeSet){

    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 10f
        style = Paint.Style.STROKE
    }
    private val rectOverlay = MutableLiveData<Rect>(Rect(0,0,0,0))

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawRect(rectOverlay.value!!, paint)
    }

    fun updateOverlay(rect: Rect){
        rectOverlay.value = rect
        invalidate()
    }
}