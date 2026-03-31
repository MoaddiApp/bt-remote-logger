package com.btremotelogger.keyevent

import android.view.KeyEvent
import android.view.InputDevice
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule

class KeyEventModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String = "KeyEventListener"

  @ReactMethod
  fun startListening() {
    instance = this
  }

  @ReactMethod
  fun stopListening() {
    if (instance == this) {
      instance = null
    }
  }

  @ReactMethod
  fun addListener(eventName: String) {
    // Required for RN NativeEventEmitter
  }

  @ReactMethod
  fun removeListeners(count: Int) {
    // Required for RN NativeEventEmitter
  }

  fun emitKeyEvent(keyCode: Int, action: Int, scanCode: Int, source: Int, deviceId: Int, deviceName: String) {
    val actionStr = when (action) {
      KeyEvent.ACTION_DOWN -> "KEY_DOWN"
      KeyEvent.ACTION_UP -> "KEY_UP"
      else -> "ACTION_$action"
    }

    val keyName = resolveKeyName(keyCode)
    val sourceName = resolveSourceName(source)

    val params = Arguments.createMap().apply {
      putInt("keyCode", keyCode)
      putString("keyName", keyName)
      putString("action", actionStr)
      putString("source", sourceName)
      putDouble("timestamp", System.currentTimeMillis().toDouble())
      putBoolean("isConsumed", true)
      putInt("scanCode", scanCode)
      putInt("deviceId", deviceId)
      putString("deviceName", deviceName)
    }

    reactApplicationContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit("onKeyEvent", params)
  }

  private fun resolveKeyName(keyCode: Int): String {
    return when (keyCode) {
      KeyEvent.KEYCODE_DPAD_UP -> "DPAD_UP"
      KeyEvent.KEYCODE_DPAD_DOWN -> "DPAD_DOWN"
      KeyEvent.KEYCODE_DPAD_LEFT -> "DPAD_LEFT"
      KeyEvent.KEYCODE_DPAD_RIGHT -> "DPAD_RIGHT"
      KeyEvent.KEYCODE_DPAD_CENTER -> "DPAD_CENTER"
      KeyEvent.KEYCODE_ENTER -> "ENTER"
      KeyEvent.KEYCODE_VOLUME_UP -> "VOLUME_UP"
      KeyEvent.KEYCODE_VOLUME_DOWN -> "VOLUME_DOWN"
      KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> "MEDIA_PLAY_PAUSE"
      KeyEvent.KEYCODE_MEDIA_NEXT -> "MEDIA_NEXT"
      KeyEvent.KEYCODE_MEDIA_PREVIOUS -> "MEDIA_PREVIOUS"
      KeyEvent.KEYCODE_MEDIA_STOP -> "MEDIA_STOP"
      KeyEvent.KEYCODE_CAMERA -> "CAMERA"
      KeyEvent.KEYCODE_FOCUS -> "FOCUS"
      KeyEvent.KEYCODE_SEARCH -> "SEARCH"
      KeyEvent.KEYCODE_BACK -> "BACK"
      KeyEvent.KEYCODE_HOME -> "HOME"
      KeyEvent.KEYCODE_MENU -> "MENU"
      KeyEvent.KEYCODE_TAB -> "TAB"
      KeyEvent.KEYCODE_SPACE -> "SPACE"
      KeyEvent.KEYCODE_PAGE_UP -> "PAGE_UP"
      KeyEvent.KEYCODE_PAGE_DOWN -> "PAGE_DOWN"
      KeyEvent.KEYCODE_ESCAPE -> "ESCAPE"
      KeyEvent.KEYCODE_BUTTON_A -> "BUTTON_A"
      KeyEvent.KEYCODE_BUTTON_B -> "BUTTON_B"
      KeyEvent.KEYCODE_BUTTON_X -> "BUTTON_X"
      KeyEvent.KEYCODE_BUTTON_Y -> "BUTTON_Y"
      else -> KeyEvent.keyCodeToString(keyCode)
    }
  }

  private fun resolveSourceName(source: Int): String {
    val sources = mutableListOf<String>()
    if (source and InputDevice.SOURCE_KEYBOARD != 0) sources.add("KEYBOARD")
    if (source and InputDevice.SOURCE_DPAD != 0) sources.add("DPAD")
    if (source and InputDevice.SOURCE_GAMEPAD != 0) sources.add("GAMEPAD")
    if (source and InputDevice.SOURCE_JOYSTICK != 0) sources.add("JOYSTICK")
    if (sources.isEmpty()) sources.add("0x${Integer.toHexString(source)}")
    return sources.joinToString("|")
  }

  companion object {
    @JvmStatic
    var instance: KeyEventModule? = null
      private set
  }
}
