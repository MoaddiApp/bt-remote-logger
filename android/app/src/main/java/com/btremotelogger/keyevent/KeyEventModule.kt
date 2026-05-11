package com.btremotelogger.keyevent

import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.InputDevice
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule

class KeyEventModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String = "KeyEventListener"

  private val handler = Handler(Looper.getMainLooper())

  private var hoverStartX = 0f
  private var hoverStartY = 0f
  private var hoverLastX = 0f
  private var hoverLastY = 0f
  private var isTracking = false

  // Deferred detection: volume key and mouse click may arrive in either order
  private var pendingVolumeKey = false
  private var pendingClick = false
  private var pendingRunnable: Runnable? = null
  private val DETECT_WINDOW_MS = 400L

  private val SWIPE_THRESHOLD = 100f

  @ReactMethod
  fun startListening() {
    instance = this
  }

  @ReactMethod
  fun stopListening() {
    if (instance == this) {
      instance = null
    }
    cancelPending()
  }

  @ReactMethod
  fun addListener(eventName: String) {}

  @ReactMethod
  fun removeListeners(count: Int) {}

  fun handleKeyEvent(keyCode: Int, action: Int, deviceName: String): Boolean {
    if (action != KeyEvent.ACTION_DOWN) return true

    when (keyCode) {
      KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
        onVolumeKeyReceived()
        return true
      }
      else -> {
        return false
      }
    }
  }

  fun handleMotionEvent(event: MotionEvent) {
    when (event.actionMasked) {
      MotionEvent.ACTION_HOVER_ENTER -> {
        hoverStartX = event.x
        hoverStartY = event.y
        hoverLastX = event.x
        hoverLastY = event.y
        isTracking = true
      }
      MotionEvent.ACTION_HOVER_MOVE -> {
        if (isTracking) {
          hoverLastX = event.x
          hoverLastY = event.y
        }
      }
      MotionEvent.ACTION_HOVER_EXIT -> {
        if (isTracking) {
          evaluateGesture()
          isTracking = false
        }
      }
      11 -> { // ACTION_BUTTON_PRESS
        if (!isTracking) {
          hoverStartX = event.x
          hoverStartY = event.y
          isTracking = true
        }
      }
      12 -> { // ACTION_BUTTON_RELEASE
        if (isTracking) {
          hoverLastX = event.x
          hoverLastY = event.y
          evaluateGesture()
          isTracking = false
        }
      }
    }
  }

  fun handleTouchEvent(event: MotionEvent) {
    // Consumed in MainActivity to prevent phantom taps
  }

  private fun evaluateGesture() {
    val deltaX = hoverLastX - hoverStartX
    val deltaY = hoverLastY - hoverStartY
    val absDeltaX = Math.abs(deltaX)
    val absDeltaY = Math.abs(deltaY)

    if (absDeltaX > SWIPE_THRESHOLD || absDeltaY > SWIPE_THRESHOLD) {
      // Swipe = arrow button
      if (absDeltaY >= absDeltaX) {
        if (deltaY < 0) {
          emitButton("ARROW_UP", "Arrow Up (swipe up)")
        } else {
          emitButton("ARROW_DOWN", "Arrow Down (swipe down)")
        }
      } else {
        if (deltaX < 0) {
          emitButton("ARROW_LEFT", "Arrow Left (swipe left)")
        } else {
          emitButton("ARROW_RIGHT", "Arrow Right (swipe right)")
        }
      }
    } else {
      // No movement = click = Gear (if volume key) or Heart (if no volume key)
      onClickReceived()
    }
  }

  private fun onVolumeKeyReceived() {
    if (pendingClick) {
      // Click already arrived, volume key confirms it's GEAR
      cancelPending()
      emitButton("GEAR", "Gear button")
    } else {
      // No click yet - wait for one
      pendingVolumeKey = true
      schedulePendingResolve()
    }
  }

  private fun onClickReceived() {
    if (pendingVolumeKey) {
      // Volume key already arrived, click confirms it's GEAR
      cancelPending()
      emitButton("GEAR", "Gear button")
    } else {
      // No volume key yet - wait for one
      pendingClick = true
      schedulePendingResolve()
    }
  }

  private fun schedulePendingResolve() {
    // Only schedule if not already scheduled
    if (pendingRunnable != null) return

    pendingRunnable = Runnable {
      if (pendingVolumeKey && !pendingClick) {
        // Volume key only, no click = CAMERA
        emitButton("CAMERA", "Camera button")
      } else if (pendingClick && !pendingVolumeKey) {
        // Click only, no volume key = HEART
        emitButton("HEART", "Heart / Like button")
      } else if (pendingVolumeKey && pendingClick) {
        // Both arrived (shouldn't get here, but just in case) = GEAR
        emitButton("GEAR", "Gear button")
      }
      pendingVolumeKey = false
      pendingClick = false
      pendingRunnable = null
    }
    handler.postDelayed(pendingRunnable!!, DETECT_WINDOW_MS)
  }

  private fun cancelPending() {
    pendingRunnable?.let {
      handler.removeCallbacks(it)
      pendingRunnable = null
    }
    pendingVolumeKey = false
    pendingClick = false
  }

  private fun emitButton(buttonId: String, label: String) {
    val params = Arguments.createMap().apply {
      putString("buttonId", buttonId)
      putString("label", label)
      putDouble("timestamp", System.currentTimeMillis().toDouble())
    }

    try {
      reactApplicationContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        .emit("onButtonDetected", params)
    } catch (e: Exception) {
      // Context may not be ready
    }
  }

  companion object {
    @JvmStatic
    var instance: KeyEventModule? = null
      private set
  }
}
