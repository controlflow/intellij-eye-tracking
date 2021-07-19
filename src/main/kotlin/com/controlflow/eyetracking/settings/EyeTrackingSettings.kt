package com.controlflow.eyetracking.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "EyeTrackingPluginSettings", storages = [Storage("eyeTrackingPlugin.xml")])
class EyeTrackingSettings : PersistentStateComponent<EyeTrackingSettings.State> {
  private var state: State = State()

  companion object {
    val instance get() : State = ServiceManager.getService(EyeTrackingSettings::class.java).state
  }

  override fun getState(): State = state
  override fun loadState(state: State) {
    this.state = state
  }

  data class State(
    var isEnabled: Boolean = true,
    var preferredMonitor: String? = null, // todo: inner type
    var text: String = ""
  )
}