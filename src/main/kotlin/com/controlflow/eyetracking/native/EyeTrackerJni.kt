package com.controlflow.eyetracking.native

object EyeTrackerJni {
  external fun initializeApi(): String
  external fun freeApi(): String

  external fun listDevices(): Array<String>
  external fun connectDevice(device: String): String
  external fun disconnectDevice(): String

  private external fun receivePosition(): Long

  fun receive(): Pair<Float, Float> {
    val positionEncoded = receivePosition()
    val x = Float.fromBits(positionEncoded.shr(32).toInt())
    val y = Float.fromBits(positionEncoded.toInt())
    return Pair(x, y)
  }
}