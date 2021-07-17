package com.controlflow.eyetracking.services

import com.intellij.openapi.project.Project
import com.controlflow.eyetracking.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
