package com.controlflow.eyetracking.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo

class EyeTrackingApplicationService : Disposable {
  companion object {
    val instance: EyeTrackingApplicationService
      get() = ServiceManager.getService(EyeTrackingApplicationService::class.java)
  }

  private val logger: Logger = Logger.getInstance(EyeTrackingApplicationService::class.java)
  var initializationProblem : String? = null

  init {
    initializationProblem = tryLoadLibraries()
  }

  private fun tryLoadLibraries() : String? {
    if (!SystemInfo.isWindows)
      return "Only Windows operating system is supported\n(Tobii drivers limitation)"
    if (!SystemInfo.is64Bit)
      return "Only 64-bit version of Windows is supported\n(Tobii drivers limitation)"

    fun loadNative(libName: String) : String? {
      return try {
        System.loadLibrary(libName)
        null
      } catch (throwable : Throwable) {
        logger.error(throwable)
        "Failed to load '$libName' native library"
      }
    }

    return loadNative("tobii_stream_engine")
      ?: loadNative("intellij_eye_tracking_jni")
  }

//    val result1 = EyeTrackerJni.initializeApi()
//    val result2 = EyeTrackerJni.listDevices()
//
//    val result3 = EyeTrackerJni.connectDevice(result2[1])
//    val (first, second) = EyeTrackerJni.receive()

  override fun dispose() {

  }
}