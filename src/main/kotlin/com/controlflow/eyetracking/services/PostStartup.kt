package com.controlflow.eyetracking.services

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class PostStartup : StartupActivity.Background, DumbAware {
  override fun runActivity(project: Project) {

    // report error
    val service = EyeTrackingApplicationService.instance

    val initializationProblem = service.initializationProblem
    if (initializationProblem != null) {
      EyeTrackingNotifications.initializationFailed(initializationProblem)


    }
  }
}