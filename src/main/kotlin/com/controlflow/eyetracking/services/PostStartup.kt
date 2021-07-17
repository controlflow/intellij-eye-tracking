package com.controlflow.eyetracking.services

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity


class PostStartup : StartupActivity {
    override fun runActivity(project: Project) {


        val notification = Notification(
            "eyetracking-plugin",
            "Eye tracking",
            "Device connected successfully",
            NotificationType.INFORMATION)
            .setIcon(icons.PlatformImplIcons.Android);

        Notifications.Bus.notify(notification)

        System.loadLibrary("tobii_stream_engine")
        System.loadLibrary("intellij_eye_tracking_jni")
    }

}