package com.controlflow.eyetracking.services

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import icons.EyeTrackingIcons
import javax.swing.Timer

object EyeTrackingNotifications {
  private const val title = "Eye Tracking plugin"
  private val notificationGroup: NotificationGroup =
    NotificationGroup(title, NotificationDisplayType.BALLOON, icon = EyeTrackingIcons.Main)

  fun initializationFailed(message: String) {
    val notification = notificationGroup.createNotification(
      title, message, NotificationType.ERROR
    )
    notification.subtitle = "initialization failed"

    Notifications.Bus.notify(notification)
  }

  fun deviceConnected(message: String) {
    val notification = notificationGroup.createNotification(
      title, message, NotificationType.INFORMATION
    )
    notification.subtitle = "device connected"
    notification.icon = EyeTrackingIcons.Main

    Notifications.Bus.notify(notification)

    val timer = Timer(2500) { notification.expire() }
    timer.isRepeats = false
    timer.start()
  }

  fun connectionProblem(message: String) {
    val notification = notificationGroup.createNotification(
      title, message, NotificationType.ERROR
    )
    notification.subtitle = "connectivity problem"

    Notifications.Bus.notify(notification)

    val timer = Timer(5000) { notification.expire() }
    timer.isRepeats = false
    timer.start()
  }
}