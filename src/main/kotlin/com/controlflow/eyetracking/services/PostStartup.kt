package com.controlflow.eyetracking.services

import com.controlflow.eyetracking.native.EyeTrackerJni
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.SystemInfo
import com.sun.jna.Library
import javax.swing.Icon


class PostStartup : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        if (SystemInfo.isWindows) {
            if (SystemInfo.is64Bit) {

            }
        }

        val notification = Notification(
            "eyetracking-plugin",
            "Eye tracking",
            "Device connected successfully",
            NotificationType.INFORMATION)

        Notifications.Bus.notify(notification)

        System.loadLibrary("tobii_stream_engine")
        System.loadLibrary("intellij_eye_tracking_jni")

        val result1 = EyeTrackerJni.initializeApi()
        val result2 = EyeTrackerJni.listDevices()

        val result3 = EyeTrackerJni.connectDevice(result2[1])
        val (first, second) = EyeTrackerJni.receive()


    }
}