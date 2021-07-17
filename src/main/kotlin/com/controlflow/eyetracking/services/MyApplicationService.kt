package com.controlflow.eyetracking.services

import com.controlflow.eyetracking.MyBundle
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import javax.swing.UIManager

class MyApplicationService {

    init {
        println(MyBundle.message("applicationService"))

    }
}

class EyeTrackerJni
{
    external fun initializeApi(): String?
    external fun freeApi(): String?
    external fun listDevices(): Array<String?>?
    external fun connectDevice(device: String?): String?
    external fun disconnectDevice(): String?
    external fun receivePosition(): Long

    init {
        System.loadLibrary("tobii_stream_engine")
        System.loadLibrary("intellij_eye_tracking_jni")
    }
}