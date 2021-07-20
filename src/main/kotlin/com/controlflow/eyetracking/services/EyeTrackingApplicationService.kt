package com.controlflow.eyetracking.services

import com.controlflow.eyetracking.settings.EyeTrackingSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.wm.IdeFrame
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.WindowManagerListener
import java.awt.Component
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.event.HierarchyBoundsListener
import java.awt.event.HierarchyEvent
import javax.swing.SwingUtilities

class EyeTrackingApplicationService : Disposable {
  companion object {
    val instance: EyeTrackingApplicationService
      get() = ServiceManager.getService(EyeTrackingApplicationService::class.java)
  }

  private val myLogger: Logger = Logger.getInstance(EyeTrackingApplicationService::class.java)
  private var myTrackerThread: EyeTrackerThread? = null

  val initializationProblem: String?

  init {
    initializationProblem = tryLoadTrackerLibraries()

    if (initializationProblem == null) {
      val trackerThread = EyeTrackerThread(EyeTrackingSettings.instance)
      trackerThread.start()
      myTrackerThread = trackerThread

      SwingUtilities.invokeLater {
        // suspend eye tracker thread when IntelliJ is not active
        /*
        ApplicationActivationListener.TOPIC.subscribe(this, object : ApplicationActivationListener {
          override fun applicationActivated(ideFrame: IdeFrame) = trackerThread.requestResume()
          override fun delayedApplicationDeactivated(ideFrame: Window) = trackerThread.requestSuspend()
        })
        */

        val windowManager = WindowManager.getInstance()

        //val graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        //graphicsEnvironment.screenDevices.

        fun updateComponentHandler(component: Component) {
          val location = component.locationOnScreen
          val size = component.size
          val bounds = component.graphicsConfiguration.bounds


          trackerThread.update(component, ScreenRectHandler(
            location.y / bounds.height.toFloat(),
            location.x / bounds.width.toFloat(),
            (location.y + size.height) / bounds.height.toFloat(),
            (location.x + size.width) / bounds.width.toFloat())
          {

          })
        }


        val componentListener = object : ComponentListener {
          override fun componentResized(e: ComponentEvent) = updateComponentHandler(e.component)
          override fun componentMoved(e: ComponentEvent) = updateComponentHandler(e.component)
          override fun componentShown(e: ComponentEvent) { }
          override fun componentHidden(e: ComponentEvent) { }
        }

        val hierarchyBoundsListener = object : HierarchyBoundsListener {
          override fun ancestorMoved(e: HierarchyEvent) = updateComponentHandler(e.component)
          override fun ancestorResized(e: HierarchyEvent?) {}
        }



        // subscribe all the opened frames
        for (ideFrame in windowManager.allProjectFrames) {
          ideFrame.component.addComponentListener(componentListener)
          ideFrame.component.addHierarchyBoundsListener(hierarchyBoundsListener)
          updateComponentHandler(ideFrame.component)
        }

        // and also
        windowManager.addListener(object : WindowManagerListener {
          override fun frameCreated(frame: IdeFrame) {
            frame.component.addComponentListener(componentListener)
            frame.component.addHierarchyBoundsListener(hierarchyBoundsListener)
          }

          override fun beforeFrameReleased(frame: IdeFrame) {
            frame.component.removeComponentListener(componentListener)
            frame.component.removeHierarchyBoundsListener(hierarchyBoundsListener)
          }
        })
      }




    }


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


    //ApplicationManager.getApplication().isActive

    //Disposer.

    // try initialize

    // todo: look for existing and subscribe to IDE frames add/remove
    // todo: choose single monitor based on recognition settings

//    ApplicationManager.getApplication().invokeLater({
//
//    }, ModalityState.any())
  }

  fun subscribe() {

  }

  private fun tryLoadTrackerLibraries(): String? {
    if (!SystemInfo.isWindows)
      return "Only Windows operating system is supported\n(Tobii drivers limitation)"
    if (!SystemInfo.is64Bit)
      return "Only 64-bit version of Windows is supported\n(Tobii drivers limitation)"

    fun loadNative(libName: String): String? {
      return try {
        System.loadLibrary(libName)
        null
      } catch (throwable: Throwable) {
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


data class ScreenRectHandler(
  val top : Float,
  val left : Float,
  val bottom : Float,
  val right : Float,
  val handler: () -> Unit)

