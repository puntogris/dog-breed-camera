package com.puntogris.dogbreedcamera.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.puntogris.dogbreedcamera.R

class Overlay(context: Context, attributeSet: AttributeSet) : View(context, attributeSet){

    private val rectOverlay = MutableLiveData<Rect>(Rect(0,0,0,0))

    private val paint = Paint().apply {
        color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getColor(R.color.colorAccent)
        }else {
            context.resources.getColor(R.color.colorAccent)
        }

        strokeWidth = 10f
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawRect(rectOverlay.value!!, paint)
    }

    fun updateOverlay(rect: Rect){
        rectOverlay.value = rect
        invalidate()
    }
}