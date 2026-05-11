package com.btremotelogger

import android.view.KeyEvent
import android.view.MotionEvent
import android.view.InputDevice
import com.btremotelogger.keyevent.KeyEventModule
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

class MainActivity : ReactActivity() {

  override fun getMainComponentName(): String = "BtRemoteLogger"

  override fun createReactActivityDelegate(): ReactActivityDelegate =
      DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)

  override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    val module = KeyEventModule.instance
    if (module != null) {
      val consumed = module.handleKeyEvent(
        event.keyCode,
        event.action,
        event.device?.name ?: "unknown"
      )
      if (consumed) {
        return true
      }
    }
    return super.dispatchKeyEvent(event)
  }

  override fun dispatchTouchEvent(event: MotionEvent): Boolean {
    val module = KeyEventModule.instance
    if (module != null) {
      val isExternal = event.device?.let { device ->
        val deviceName = device.name ?: ""
        deviceName.isNotEmpty() && deviceName != "unknown" &&
            (event.source and InputDevice.SOURCE_MOUSE != 0 ||
             event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE)
      } ?: false

      if (isExternal) {
        module.handleTouchEvent(event)
        return true
      }
    }
    return super.dispatchTouchEvent(event)
  }

  override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
    val module = KeyEventModule.instance
    if (module != null) {
      val deviceName = event.device?.name ?: "unknown"
      if (deviceName.isNotEmpty() && deviceName != "unknown") {
        module.handleMotionEvent(event)
        return true
      }
    }
    return super.dispatchGenericMotionEvent(event)
  }
}
