package com.controlflow.eyetracking

import com.intellij.AbstractBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.MyBundle"

public object MyBundle : AbstractBundle(BUNDLE) {

  @Suppress("SpreadOperator")
  @JvmStatic
  public fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) = getMessage(key, *params)

  @Suppress("SpreadOperator")
  @JvmStatic
  public fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) = run {
    message(key, *params)
  }
}
