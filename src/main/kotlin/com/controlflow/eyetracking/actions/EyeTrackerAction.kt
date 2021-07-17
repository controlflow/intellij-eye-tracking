package com.controlflow.eyetracking.actions

import com.controlflow.eyetracking.settings.EyeTrackingPluginSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

public class EyeTrackerAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {

        val enabled = EyeTrackingPluginSettings.instance.isEnabled

        Messages.showInfoMessage("Hi, text: ${EyeTrackingPluginSettings.instance.text}", "Test, enabled: $enabled")


        EyeTrackingPluginSettings.instance.isEnabled = !enabled
        EyeTrackingPluginSettings.instance.text = "aaa"

        //TODO("Not yet implemented")
    }
}