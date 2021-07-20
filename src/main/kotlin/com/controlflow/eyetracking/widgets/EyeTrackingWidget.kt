package com.controlflow.eyetracking.widgets

import com.intellij.application.subscribe
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.TextPanel
import com.intellij.util.messages.Topic
import javax.swing.JComponent

class EyeTrackingWidget : CustomStatusBarWidget {
  companion object {
    var TOPIC: Topic<WidgetText> = Topic("EyeTrackingWidget", WidgetText::class.java)
  }

  private var myStatusBar: StatusBar? = null

  override fun ID(): String = "EyeTracking"

  override fun install(statusBar: StatusBar) {
    myStatusBar = statusBar
    Disposer.register(statusBar, this)
  }

  override fun getComponent(): JComponent {
    val panel = TextPanel.WithIconAndArrows()



    TOPIC.subscribe(this, object : WidgetText {
      override fun set(text: String) {
        panel.text = text
      }
    })

    panel.text = "init"
    return panel
  }

  override fun dispose() {
    myStatusBar = null
  }

  override fun getPresentation(): StatusBarWidget.WidgetPresentation? = null
}