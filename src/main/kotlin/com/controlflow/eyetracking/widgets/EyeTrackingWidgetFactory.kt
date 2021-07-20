package com.controlflow.eyetracking.widgets

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

class EyeTrackingWidgetFactory : StatusBarWidgetFactory {
  override fun getId(): String = "EyeTracking"
  override fun getDisplayName(): String = "Eye Tracking"

  override fun isAvailable(project: Project): Boolean = true;

  override fun createWidget(project: Project): StatusBarWidget = EyeTrackingWidget()

  override fun disposeWidget(widget: StatusBarWidget) {
    Disposer.dispose(widget)
  }

  override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}

interface WidgetText {
  fun set(text: String)
}

