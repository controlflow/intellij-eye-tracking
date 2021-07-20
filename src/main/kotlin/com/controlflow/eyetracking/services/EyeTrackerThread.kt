package com.controlflow.eyetracking.services

import com.controlflow.eyetracking.native.EyeTrackerJni
import com.controlflow.eyetracking.settings.EyeTrackingSettings
import com.controlflow.eyetracking.widgets.EyeTrackingWidget
import com.intellij.openapi.application.ApplicationManager
import javax.swing.SwingUtilities

// todo: must re-create on settings change
// todo: must restart on device disconnect
// todo: must receive the tree structure

class EyeTrackerThread(private val settings: EyeTrackingSettings.State) : Thread("EyeTrackerPluginThread") {

  private var myState: ThreadState = ThreadState.BeforeConnect
  private val myHandlersMap: MutableMap<Any, ScreenRectHandler> = mutableMapOf()
  private var isSuspended: Boolean = false
  private var wasInitialized: Boolean = false
  private var wasConnected: Boolean = false

  private enum class ThreadState {
    BeforeConnect, // before .start() is called
    ReceivingGazeStream, // connection is fine, streaming gaze positions
    FailedToConnect, // connection failed, retry connection in 10 second intervals
    Suspended, // connection is fine, but any of the IDE frames are not active
    DisposeRequested // thread disposal requested, must end execution
  }

  // todo: pause when not focused?
  // todo: pause when there are no project frames

  override fun run() {
    while (true) {
      when (synchronized(this) { myState }) {
        ThreadState.BeforeConnect -> connect(reportFailures = true)
        ThreadState.ReceivingGazeStream -> processGaze()
        ThreadState.FailedToConnect -> attemptToReconnect()
        ThreadState.Suspended -> {
          sleep(1000)
        }
        ThreadState.DisposeRequested -> {
          disconnect()
          break
        }
      }
    }
  }

  @Synchronized
  private fun connect(reportFailures: Boolean) {
    assert(!wasInitialized)
    assert(!wasConnected)

    val initializationResult = EyeTrackerJni.initializeApi()
    if (initializationResult != "") {
      myState = ThreadState.FailedToConnect
      if (reportFailures)
        reportConnectionProblem("Tobii API initialization failed: $initializationResult")
      return
    }

    wasInitialized = true

    val trackingDevices = EyeTrackerJni.listDevices() ?: emptyArray()
    if (trackingDevices.isEmpty()) {
      myState = ThreadState.FailedToConnect
      if (reportFailures)
        reportConnectionProblem("Tobii device list request failed")
      return
    }

    val deviceRequestResult = trackingDevices[0]
    if (trackingDevices.size == 1 && deviceRequestResult == "") {
      myState = ThreadState.FailedToConnect
      if (reportFailures)
        reportConnectionProblem("No connected Tobii device found")
      return
    }

    if (deviceRequestResult != "") {
      myState = ThreadState.FailedToConnect
      if (reportFailures)
        reportConnectionProblem("Tobii devices list request failed: $deviceRequestResult")
      return
    }

    val deviceUrl = trackingDevices[1]
    val connectionResult = EyeTrackerJni.connectDevice(deviceUrl)
    if (connectionResult != "") {
      myState = ThreadState.FailedToConnect
      if (reportFailures)
        reportConnectionProblem("Tobii device connection failed: $connectionResult")
      return
    }

    wasConnected = true
    myState = ThreadState.ReceivingGazeStream
    reportSuccessfulConnection("Streaming gaze data from '$deviceUrl' device")
  }

  // note: not synchronized to allow faster suspend/dispose
  private fun attemptToReconnect() {
    // 5 seconds pause
    for (index in 0..10) {
      sleep(500)
      if (myState != ThreadState.FailedToConnect) return
    }

    disconnect()
    connect(reportFailures = false)
  }

  @Synchronized
  private fun disconnect() {
    if (wasConnected) {
      EyeTrackerJni.disconnectDevice()
    }

    wasConnected = false

    if (wasInitialized) {
      EyeTrackerJni.freeApi()
    }

    wasInitialized = false
  }

  @Synchronized
  fun requestSuspend() {
    if (myState == ThreadState.ReceivingGazeStream) {
      myState = ThreadState.Suspended
    }
  }

  @Synchronized
  fun requestResume() {
    if (myState == ThreadState.Suspended) {
      myState = ThreadState.ReceivingGazeStream
    }
  }

  @Synchronized
  fun requestDispose() {
    myState = ThreadState.DisposeRequested
  }

  @Synchronized
  private fun processGaze() {
    val position = EyeTrackerJni.receivePosition()
    val x = Float.fromBits(position.ushr(32).toInt())
    val y = Float.fromBits(position.toInt())

    if (x.isNaN() || y.isNaN()) {
      // probably disconnected
      reportConnectionProblem("Gaze data stream interrupted")
      disconnect()
      myState = ThreadState.FailedToConnect
    }

      //SwingUtilities.invokeLater {
      //ApplicationManager.getApplication().messageBus.syncPublisher(EyeTrackingWidget.TOPIC).set("x: $x y: $y")
    //}

    synchronized (myHandlersMap) {
      for ((_, value) in myHandlersMap) {
        if (x >= value.left
            && x <= value.right
            && y >= value.top
            && y <= value.bottom) {
          SwingUtilities.invokeLater {
            ApplicationManager.getApplication().messageBus
              .syncPublisher(EyeTrackingWidget.TOPIC)
              .set("I SEE YOU'RE LOOKING AT ME")
          }
          return
        }
      }

      SwingUtilities.invokeLater {
        ApplicationManager.getApplication().messageBus
          .syncPublisher(EyeTrackingWidget.TOPIC)
          .set("Where are you?")
      }
    }


    // todo: match coordinates against data structure
  }

  private fun reportConnectionProblem(message: String) {
    SwingUtilities.invokeLater {
      EyeTrackingNotifications.connectionProblem(message)
    }
  }

  private fun reportSuccessfulConnection(message: String) {
    SwingUtilities.invokeLater {
      EyeTrackingNotifications.deviceConnected(message)
    }
  }

  fun update(key : Any, handler: ScreenRectHandler) {
    synchronized (myHandlersMap) {
      myHandlersMap[key] = handler
    }
  }
}