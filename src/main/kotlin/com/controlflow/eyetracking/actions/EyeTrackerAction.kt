package com.controlflow.eyetracking.actions

import com.controlflow.eyetracking.services.EyeTrackingApplicationService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.wm.WindowManager
import java.awt.GraphicsEnvironment
import java.awt.event.FocusEvent
import java.awt.event.FocusListener

public class EyeTrackerAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {


//    ApplicationManager.getApplication().messageBus
//      .syncPublisher(EyeTrackingApplicationService.Topic)
//      .bar("AAAAA")

    val localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()

    for (screenDevice in localGraphicsEnvironment.screenDevices) {
      //val a = screenDevice.displayModes
    }

    val applicationService = ServiceManager.getService(EyeTrackingApplicationService::class.java)

    val visibleFrame = WindowManager.getInstance().findVisibleFrame()
    if (visibleFrame != null) {
      val locationOnScreen = visibleFrame.locationOnScreen

    }

    WindowManager.getInstance().findVisibleFrame().addFocusListener(object : FocusListener {
      override fun focusGained(e: FocusEvent?) {

        //TODO("Not yet implemented")
      }

      override fun focusLost(e: FocusEvent?) {
        //TODO("Not yet implemented")
      }

    })

    //SwingUtilities.

    //val enabled = EyeTrackingPluginSettings.instance.isEnabled

//        Messages.showInfoMessage("Hi, text: ${EyeTrackingPluginSettings.instance.text}", "Test, enabled: $enabled")
//
//
//        EyeTrackingPluginSettings.instance.isEnabled = !enabled
//        EyeTrackingPluginSettings.instance.text = "aaa"

    //TODO("Not yet implemented")
  }
}