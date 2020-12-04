package com.github.controlflow.intellijeyetracker.services

import com.intellij.openapi.project.Project
import com.github.controlflow.intellijeyetracker.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
