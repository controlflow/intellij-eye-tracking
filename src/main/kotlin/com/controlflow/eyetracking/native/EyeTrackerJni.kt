package com.controlflow.eyetracking.native

object EyeTrackerJni {
  external fun initializeApi(): String
  external fun freeApi(): String

  external fun listDevices(): Array<String>?
  external fun connectDevice(device: String): String
  external fun disconnectDevice(): String

  external fun receivePosition(): Long
}