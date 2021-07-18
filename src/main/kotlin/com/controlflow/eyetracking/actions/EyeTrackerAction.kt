package com.controlflow.eyetracking.actions

import com.controlflow.eyetracking.services.EyeTrackingApplicationService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.wm.WindowManager
import java.awt.GraphicsEnvironment

public class EyeTrackerAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {

    val localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()

    for (screenDevice in localGraphicsEnvironment.screenDevices) {
      val a = screenDevice.displayModes

      val b = a
    }

    val applicationService = ServiceManager.getService(EyeTrackingApplicationService::class.java)

//Toolkit.getDefaultToolkit().screenSize

    val visibleFrame = WindowManager.getInstance().findVisibleFrame()
    if (visibleFrame != null) {
      val locationOnScreen = visibleFrame.locationOnScreen

    }

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