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

