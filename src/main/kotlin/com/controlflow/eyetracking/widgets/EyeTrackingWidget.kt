package com.controlflow.eyetracking.widgets

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.impl.status.TextPanel
import javax.swing.JComponent

class EyeTrackingWidget : StatusBarWidgetFactory {
  override fun getId(): String = "EyeTracking"
  override fun getDisplayName(): String = "Eye Tracking"

  override fun isAvailable(project: Project): Boolean = true;

  override fun createWidget(project: Project): StatusBarWidget = MyWidget()

  override fun disposeWidget(widget: StatusBarWidget) {
    Disposer.dispose(widget)
  }

  override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}

class MyWidget : CustomStatusBarWidget {
  //private var myDisposed: Boolean = false
  private var myStatusBar: StatusBar? = null
  //private var myPanel : TextPanel.WithIconAndArrows? = null
  private var myText: String = ""

  override fun ID(): String = "EyeTracking"

  override fun install(statusBar: StatusBar) {
    //TextPanel.WithIconAndArrows()

    myStatusBar = statusBar
    Disposer.register(statusBar, this)
  }



  override fun getComponent(): JComponent {


    val panel = TextPanel.WithIconAndArrows()

//    ApplicationManager.getApplication().messageBus.connect(this)
//      .subscribe(EyeTrackingApplicationService.Topic,
//      object : I {
//        override fun bar(s: String) {
//          //myText = te
//          panel.text = s;
//
//          //myStatusBar!!.updateWidget(ID())
//        }
//      })

    panel.text = "init"
    return panel
  }

  override fun dispose() {
    //myDisposed = true
    //myStatusBar = null
    //myPanel = null
  }

  override fun getPresentation(): StatusBarWidget.WidgetPresentation? = null
}