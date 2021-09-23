package com.puntogris.dogbreedcamera.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import com.puntogris.dogbreedcamera.R

class CameraOverlay(context: Context, attributeSet: AttributeSet) : View(context, attributeSet){

    private val rectOverlay = MutableLiveData(Rect(0,0,0,0))

    private val paint = Paint().apply {
        color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
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