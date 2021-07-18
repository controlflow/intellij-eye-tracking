package com.controlflow.eyetracking.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "EyeTrackingPluginSettings", storages = [Storage("eyeTrackingPlugin.xml")])
class EyeTrackingPluginSettings : PersistentStateComponent<EyeTrackingPluginSettings.PluginSetting> {
  private var state: PluginSetting = PluginSetting()

  companion object {
    val instance get() : PluginSetting = ServiceManager.getService(EyeTrackingPluginSettings::class.java).state
  }

  override fun getState(): PluginSetting = state
  override fun loadState(state: PluginSetting) {
    this.state = state
  }

  data class PluginSetting(
    var isEnabled: Boolean = true,
    var text: String = ""
  )
}