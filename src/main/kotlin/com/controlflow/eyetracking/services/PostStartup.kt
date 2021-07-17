package com.controlflow.eyetracking.services

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.sun.jna.Library
import javax.swing.Icon


class PostStartup : StartupActivity {
    override fun runActivity(project: Project) {


        val notification = Notification(
            "eyetracking-plugin",
            "Eye tracking",
            "Device connected successfully",
            NotificationType.INFORMATION)
        //    .setIcon(Icon)
        ;

        Notifications.Bus.notify(notification)

        //com.sun.jna.win32.
        //User32.INSTANCE;

        //val loaded = com.sun.jna.Native.load("tobii_stream_engine", CLibrary::class.java)



        System.loadLibrary("tobii_stream_engine")
        System.loadLibrary("intellij_eye_tracking_jni")

        val result1 = EyeTrackerJni.initializeApi();

    }

    object EyeTrackerJni
    {
        @JvmStatic
        external fun initializeApi(): String?
        @JvmStatic
        external fun freeApi(): String?
        @JvmStatic
        external fun listDevices(): Array<String?>?
        @JvmStatic
        external fun connectDevice(device: String?): String?
        @JvmStatic
        external fun disconnectDevice(): String?
        @JvmStatic
        external fun receivePosition(): Long
    }
}