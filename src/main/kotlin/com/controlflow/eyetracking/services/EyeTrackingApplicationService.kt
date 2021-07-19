package com.controlflow.eyetracking.services

import com.controlflow.eyetracking.settings.EyeTrackingSettings
import com.intellij.application.subscribe
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.wm.IdeFrame
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.WindowManagerListener
import java.awt.Window

class EyeTrackingApplicationService : Disposable {
  companion object {
    val instance: EyeTrackingApplicationService
      get() = ServiceManager.getService(EyeTrackingApplicationService::class.java)
  }

  private val myLogger : Logger = Logger.getInstance(EyeTrackingApplicationService::class.java)
  private var myTrackerThread : EyeTrackerThread? = null

  val initializationProblem : String?

  init {
    //ProcessM
    // todo: assert on UI thread?

    initializationProblem = tryLoadTrackerLibraries()

    if (initializationProblem == null) {
      val trackerThread = EyeTrackerThread(EyeTrackingSettings.instance)
      trackerThread.start()
      myTrackerThread = trackerThread
    }

//    FrameStateListener.TOPIC.subscribe(this, object : FrameStateListener {
//      override fun onFrameActivated() {
//        super.onFrameActivated()
//      }
//    })
//FrameStateManager.getInstance().addListener()

    //WindowManager.getInstance().

    WindowManager.getInstance().addListener(object : WindowManagerListener {
      override fun frameCreated(frame: IdeFrame) {
        // subscribe new frame
      }

      override fun beforeFrameReleased(frame: IdeFrame) {
        val findVisibleFrame = WindowManager.getInstance().findVisibleFrame()

        val a = findVisibleFrame
        // unsubscribe frame
      }
    })

    ApplicationActivationListener.TOPIC.subscribe(this, object : ApplicationActivationListener {
      override fun applicationActivated(ideFrame: IdeFrame) {
        // suspend thread

        //super.applicationActivated(ideFrame)
      }

      override fun delayedApplicationDeactivated(ideFrame: Window) {
        // resume thread
        //super.delayedApplicationDeactivated(ideFrame)
      }
    })

    //ApplicationManager.getApplication().isActive

    //Disposer.

    // try initialize

    // todo: look for existing and subscribe to IDE frames add/remove
    // todo: choose single monitor based on recognition settings

//    ApplicationManager.getApplication().invokeLater({
//
//    }, ModalityState.any())
  }

  private fun tryLoadTrackerLibraries() : String? {
    if (!SystemInfo.isWindows)
      return "Only Windows operating system is supported\n(Tobii drivers limitation)"
    if (!SystemInfo.is64Bit)
      return "Only 64-bit version of Windows is supported\n(Tobii drivers limitation)"

    fun loadNative(libName: String) : String? {
      return try {
        System.loadLibrary(libName)
        null
      } catch (throwable : Throwable) {
        myLogger.error(throwable)
        "Failed to load '$libName' native library"
      }
    }

    return loadNative("tobii_stream_engine")
      ?: loadNative("intellij_eye_tracking_jni")
  }

  override fun dispose() {
    val trackerThread = myTrackerThread
    if (trackerThread != null) {
      trackerThread.requestDispose()
      trackerThread.join()
    }
  }
}



