package com.controlflow.eyetracking.services

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import icons.EyeTrackingIcons

object PluginNotifications {
  val notificationGroup : NotificationGroup =
    NotificationGroup("Eye Tracking plugin", NotificationDisplayType.BALLOON, icon = EyeTrackingIcons.Main)
}