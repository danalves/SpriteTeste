package com.danilovieira.spriteteste

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), Runnable {
    private var gameThread: Thread? = null
    private val ourHolder: SurfaceHolder
    @Volatile
    private var playing: Boolean = false
    private var canvas: Canvas? = null
    private var bitmapRunningMan: Bitmap? = null
    private var isMoving: Boolean = false
    private val runSpeedPerSecond = 500f
    private var manXPos = 10f
    private var manYPos = 10f
    private val frameWidth = 230
    private val frameHeight = 274
    private val frameCount = 8
    private var currentFrame = 0
    private var fps: Long = 0
    private var timeThisFrame: Long = 0
    private var lastFrameChangeTime: Long = 0
    private val frameLengthInMillisecond = 50
    private val frameToDraw = Rect(0, 0, frameWidth, frameHeight)
    private val whereToDraw = RectF(manXPos, manYPos, manXPos + frameWidth, frameHeight.toFloat())

    init {
        ourHolder = holder
        bitmapRunningMan = BitmapFactory.decodeResource(resources, R.drawable.running_man)
        bitmapRunningMan = Bitmap.createScaledBitmap(bitmapRunningMan!!, frameWidth * frameCount, frameHeight, false)
    }

    override fun run() {
        while (playing) {
            val startFrameTime = System.currentTimeMillis()
            update()
            draw()
            timeThisFrame = System.currentTimeMillis() - startFrameTime
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame
            }
        }
    }

    fun update() {
        if (isMoving) {
            manXPos = manXPos + runSpeedPerSecond / fps
            if (manXPos > width) {
                manYPos += frameHeight.toFloat()
                manXPos = 10f
            }
            if (manYPos + frameHeight > height) {
                manYPos = 10f
            }
        }
    }

    fun manageCurrentFrame() {
        val time = System.currentTimeMillis()
        if (isMoving) {
            if (time > lastFrameChangeTime + frameLengthInMillisecond) {
                lastFrameChangeTime = time
                currentFrame++
                if (currentFrame >= frameCount) {
                    currentFrame = 0
                }
            }
        }
        frameToDraw.left = currentFrame * frameWidth
        frameToDraw.right = frameToDraw.left + frameWidth
    }

    fun draw() {
        if (ourHolder.surface.isValid) {
            canvas = ourHolder.lockCanvas()
            canvas!!.drawColor(Color.WHITE)
            whereToDraw.set(manXPos.toInt().toFloat(), manYPos.toInt().toFloat(), (manXPos.toInt() + frameWidth).toFloat(), (manYPos.toInt() + frameHeight).toFloat())
            manageCurrentFrame()
            canvas!!.drawBitmap(bitmapRunningMan!!, frameToDraw, whereToDraw, null)
            ourHolder.unlockCanvasAndPost(canvas)
        }
    }

    fun pause() {
        playing = false
        try {
            gameThread!!.join()
        } catch (e: InterruptedException) {
            Log.e("ERR", "Joining Thread")
        }

    }

    fun resume() {
        playing = true
        gameThread = Thread(this)
        gameThread!!.start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> isMoving = !isMoving
        }
        return true
    }

}