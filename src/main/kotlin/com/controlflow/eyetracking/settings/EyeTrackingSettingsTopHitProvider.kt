package com.controlflow.eyetracking.settings

import com.intellij.ide.ui.OptionsSearchTopHitProvider
import com.intellij.ide.ui.search.BooleanOptionDescription
import com.intellij.ide.ui.search.OptionDescription

class EyeTrackingSettingsTopHitProvider : OptionsSearchTopHitProvider.ApplicationLevelProvider {
  override fun getId(): String = "eye-tracking"

  override fun getOptions(): MutableCollection<OptionDescription> {
    return mutableListOf(
      object : EyeTrackingOptionDescription("Eye Tracking Is Active") {
        override fun isOptionEnabled(): Boolean = EyeTrackingPluginSettings.instance.isEnabled
        override fun setOptionState(enabled: Boolean) {
          EyeTrackingPluginSettings.instance.isEnabled = enabled
        }
      }
    )
  }

  private abstract class EyeTrackingOptionDescription(title: String) :
    BooleanOptionDescription(title, "eye-tracking.settings")
}

