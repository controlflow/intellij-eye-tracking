#include <vector>
#include <iostream>
#include "include/jni.h"
#include "include/tobii/tobii.h"
#include "include/tobii/tobii_streams.h"
#pragma warning(disable:26812)

// globals:
tobii_api_t* global_api = nullptr;
tobii_device_t* global_device = nullptr;
float global_last_gaze_point[2] = {0, 0};
bool global_last_gaze_point_valid = false;

const char* present_tobii_error(const tobii_error_t error)
{
  switch (error)
  {
    case TOBII_ERROR_NO_ERROR:
      return "";
    case TOBII_ERROR_INTERNAL:
      return "ERROR_INTERNAL";
    case TOBII_ERROR_INSUFFICIENT_LICENSE:
      return "ERROR_INSUFFICIENT_LICENSE";
    case TOBII_ERROR_NOT_SUPPORTED:
      return "ERROR_NOT_SUPPORTED";
    case TOBII_ERROR_NOT_AVAILABLE:
      return "ERROR_NOT_AVAILABLE";
    case TOBII_ERROR_CONNECTION_FAILED:
      return "ERROR_CONNECTION_FAILED";
    case TOBII_ERROR_TIMED_OUT:
      return "ERROR_TIMED_OUT";
    case TOBII_ERROR_ALLOCATION_FAILED:
      return "ERROR_ALLOCATION_FAILED";
    case TOBII_ERROR_INVALID_PARAMETER:
      return "ERROR_INVALID_PARAMETER";
    case TOBII_ERROR_CALIBRATION_ALREADY_STARTED:
      return "ERROR_CALIBRATION_ALREADY_STARTED";
    case TOBII_ERROR_CALIBRATION_NOT_STARTED:
      return "ERROR_CALIBRATION_NOT_STARTED";
    case TOBII_ERROR_ALREADY_SUBSCRIBED:
      return "ERROR_ALREADY_SUBSCRIBED";
    case TOBII_ERROR_NOT_SUBSCRIBED:
      return "ERROR_NOT_SUBSCRIBED";
    case TOBII_ERROR_OPERATION_FAILED:
      return "ERROR_OPERATION_FAILED";
    case TOBII_ERROR_CONFLICTING_API_INSTANCES:
      return "ERROR_CONFLICTING_API_INSTANCES";
    case TOBII_ERROR_CALIBRATION_BUSY:
      return "ERROR_CALIBRATION_BUSY";
    case TOBII_ERROR_CALLBACK_IN_PROGRESS:
      return "ERROR_CALLBACK_IN_PROGRESS";
    case TOBII_ERROR_TOO_MANY_SUBSCRIBERS:
      return "ERROR_TOO_MANY_SUBSCRIBERS";
    case TOBII_ERROR_CONNECTION_FAILED_DRIVER:
      return "ERROR_CONNECTION_FAILED_DRIVER";
    case TOBII_ERROR_UNAUTHORIZED:
      return "ERROR_UNAUTHORIZED";
    case TOBII_ERROR_FIRMWARE_UPGRADE_IN_PROGRESS:
      return "ERROR_FIRMWARE_UPGRADE_IN_PROGRESS";
    default:
      return "ERROR_UNKNOWN";
  }
}

tobii_error_t initialize_eye_tracking_api()
{
  if (global_api != nullptr)
    return TOBII_ERROR_ALREADY_SUBSCRIBED;

  tobii_api_t* api = nullptr;
  const auto result = tobii_api_create(&api, nullptr, nullptr);
  if (result == TOBII_ERROR_NO_ERROR)
  {
    global_api = api;
  }

  return result;
}

tobii_error_t free_eye_tracking_api()
{
  if (global_api == nullptr)
    return TOBII_ERROR_NOT_SUBSCRIBED;

  const auto result = tobii_api_destroy(global_api);
  if (result == TOBII_ERROR_NO_ERROR)
  {
    global_api = nullptr;
  }

  return result;
}

tobii_error_t list_eye_tracking_devices(std::vector<std::string>& devices)
{
  if (global_api == nullptr)
    return TOBII_ERROR_NOT_SUBSCRIBED;

  return  tobii_enumerate_local_device_urls(
    global_api,
    [](char const* url, void* userData)
    {
      auto* devices_ptr = static_cast<std::vector<std::string>*>(userData);
      devices_ptr->push_back(std::string(url));
    },
    &devices);
}

tobii_error_t connect_eye_tracking_device(const std::string& deviceUrl)
{
  if (global_api == nullptr)
    return TOBII_ERROR_NOT_SUBSCRIBED;
  if (global_device != nullptr)
    return TOBII_ERROR_ALREADY_SUBSCRIBED;

  return tobii_device_create(
    global_api, deviceUrl.c_str(), TOBII_FIELD_OF_USE_INTERACTIVE, &global_device);
}

tobii_error_t disconnect_eye_tracking_device()
{
  if (global_api == nullptr)
    return TOBII_ERROR_NOT_SUBSCRIBED;
  if (global_device == nullptr)
    return TOBII_ERROR_NOT_SUBSCRIBED;

  const auto result = tobii_device_destroy(global_device);
  global_device = nullptr;
  return result;
}

tobii_error_t connect_eye_tracking_gaze_stream()
{
  if (global_device == nullptr)
    return TOBII_ERROR_NOT_SUBSCRIBED;

  return tobii_gaze_point_subscribe(
    global_device,
    [](tobii_gaze_point_t const* gazePoint, void*)
    {
      if (gazePoint->validity == TOBII_VALIDITY_VALID)
      {
        global_last_gaze_point[0] = gazePoint->position_xy[0];
        global_last_gaze_point[1] = gazePoint->position_xy[1];
      }
      else
      {
        global_last_gaze_point[0] = -FLT_MAX;
        global_last_gaze_point[1] = -FLT_MAX;
      }
    },
    nullptr);
}

tobii_error_t disconnect_eye_tracking_gaze_stream()
{
  if (global_device == nullptr)
    return TOBII_ERROR_NOT_SUBSCRIBED;

  return tobii_gaze_point_unsubscribe(global_device);
}

tobii_error_t wait_and_receive_eye_tracking_gaze_position()
{
  if (global_device == nullptr)
    return TOBII_ERROR_NOT_SUBSCRIBED;

  for (auto index = 0; index < 20; index++) {
    const auto wait_result = tobii_wait_for_callbacks(1, &global_device);

    if (wait_result == TOBII_ERROR_TIMED_OUT)
      continue; // loop again

    if (wait_result != TOBII_ERROR_NO_ERROR)
      return wait_result;

    const auto process_result = tobii_device_process_callbacks(global_device);
    if (process_result != TOBII_ERROR_NO_ERROR)
      return process_result;

    return process_result;
  }

  return TOBII_ERROR_TIMED_OUT;
}

/////////////////////////////////////////////////////////////////////////////////////
// ReSharper disable CppInconsistentNaming

extern "C"
{
  JNIEXPORT jstring JNICALL Java_com_controlflow_eyetracking_native_EyeTrackerJni_initializeApi(JNIEnv* env, jclass)
  {
    const auto result = initialize_eye_tracking_api();
    return env->NewStringUTF(present_tobii_error(result));
  }

  JNIEXPORT jstring JNICALL Java_com_controlflow_eyetracking_native_EyeTrackerJni_freeApi(JNIEnv* env, jclass)
  {
    const auto result = free_eye_tracking_api();
    return env->NewStringUTF(present_tobii_error(result));
  }

  JNIEXPORT jobjectArray JNICALL Java_com_controlflow_eyetracking_native_EyeTrackerJni_listDevices(JNIEnv* env, jclass)
  {
    std::vector<std::string> devices;
    const auto result = list_eye_tracking_devices(devices);

    const auto array_object = env->NewObjectArray(
      static_cast<jsize>(devices.size() + 1), env->FindClass("java/lang/String"), nullptr);

    if (array_object == nullptr) return nullptr;

    env->SetObjectArrayElement(
      array_object, 0, env->NewStringUTF(present_tobii_error(result)));

    auto index = 1;
    for (auto const& element : devices)
    {
      const auto str = env->NewStringUTF(element.c_str());
      env->SetObjectArrayElement(array_object, index++, str);
    }

    return array_object;
  }

  JNIEXPORT jstring JNICALL Java_com_controlflow_eyetracking_native_EyeTrackerJni_connectDevice(JNIEnv* env, _jclass, jstring deviceUrl)
  {
    const char* native_url = env->GetStringUTFChars(deviceUrl, nullptr);
    const auto device_result = connect_eye_tracking_device(native_url);
    env->ReleaseStringUTFChars(deviceUrl, native_url);

    if (device_result != TOBII_ERROR_NO_ERROR)
      return env->NewStringUTF(present_tobii_error(device_result)); // error

    const auto stream_result = connect_eye_tracking_gaze_stream();
    return env->NewStringUTF(present_tobii_error(stream_result));
  }

  JNIEXPORT jstring JNICALL Java_com_controlflow_eyetracking_native_EyeTrackerJni_disconnectDevice(JNIEnv* env, jclass)
  {
    const auto stream_result = disconnect_eye_tracking_gaze_stream();

    if (stream_result != TOBII_ERROR_NO_ERROR)
      return env->NewStringUTF(present_tobii_error(stream_result)); // error

    const auto device_result = disconnect_eye_tracking_device();
    return env->NewStringUTF(present_tobii_error(device_result));
  }

  JNIEXPORT jlong JNICALL Java_com_controlflow_eyetracking_native_EyeTrackerJni_receivePosition(JNIEnv*, jclass)
  {
    const auto result = wait_and_receive_eye_tracking_gaze_position();

    if (result != TOBII_ERROR_NO_ERROR)
    {
      global_last_gaze_point[0] = NAN;
      global_last_gaze_point[1] = NAN;
    }

    const auto xy = reinterpret_cast<int*>(global_last_gaze_point);
    return static_cast<jlong>(xy[0]) << 32 | xy[1]; // encode as long
  }
}

// ReSharper restore CppInconsistentNaming
/////////////////////////////////////////////////////////////////////////////////////

/*
int main()
{
  auto api_result = initialize_eye_tracking_api();
  std::vector<std::string> devices;
  auto list_result = list_eye_tracking_devices(devices);
  auto connection_result = connect_eye_tracking_device(devices[0]);
  auto stream_result = connect_eye_tracking_gaze_stream();

  for (int index = 0; index < 1000; index++)
  {
    wait_and_receive_eye_tracking_gaze_position();

    auto x = global_last_gaze_point[0];
    auto y = global_last_gaze_point[1];
    if (isnan(x))
    {
      printf("X IS NAN");
    }
    else if (isnan(y))
    {
      printf("Y IS NAN");
    }
    else
    {
      printf("Gaze point: %f, %f\n", x, y);
    }
  }

  disconnect_eye_tracking_gaze_stream();
  disconnect_eye_tracking_device();
  free_eye_tracking_api();
  return 0;
}
*/