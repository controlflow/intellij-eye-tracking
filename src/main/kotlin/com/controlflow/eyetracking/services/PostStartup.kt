package com.controlflow.eyetracking.services

import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class PostStartup : StartupActivity, DumbAware {
  override fun runActivity(project: Project) {

    // report error
    val service = EyeTrackingApplicationService.instance
    if (service.initializationProblem != null) {
      val notification = PluginNotifications.notificationGroup.createNotification(
        "Eye Tracking plugin: initialization failed",
        service.initializationProblem!!,
        NotificationType.ERROR)

      //notification.icon = EyeTrackingIcons.Main;

      Notifications.Bus.notify(notification)
    }

//    val result1 = EyeTrackerJni.initializeApi()
//    val result2 = EyeTrackerJni.listDevices()
//
//    val result3 = EyeTrackerJni.connectDevice(result2[1])
//    val (first, second) = EyeTrackerJni.receive()
  }
}