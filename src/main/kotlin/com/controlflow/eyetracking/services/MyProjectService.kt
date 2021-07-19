package com.controlflow.eyetracking.services

import com.controlflow.eyetracking.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

  init {
    println(MyBundle.message("projectService", project.name))
  }
}
