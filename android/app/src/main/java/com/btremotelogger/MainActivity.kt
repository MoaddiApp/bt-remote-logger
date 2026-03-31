package com.btremotelogger

import android.view.KeyEvent
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
      module.emitKeyEvent(
        event.keyCode,
        event.action,
        event.scanCode,
        event.source,
        event.deviceId,
        event.device?.name ?: "unknown"
      )
      if (event.keyCode != KeyEvent.KEYCODE_BACK) {
        return true
      }
    }
    return super.dispatchKeyEvent(event)
  }
}
