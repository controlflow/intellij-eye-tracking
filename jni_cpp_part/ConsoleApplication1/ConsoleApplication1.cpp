// ConsoleApplication1.cpp : This file contains the 'main' function. Program execution begins and ends there.
//

#include <cassert>
#include <vector>
#include <iostream>
#include "include/jni.h"
#include "include/tobii/tobii.h"
#include "include/tobii/tobii_streams.h"



tobii_api_t* global_api = nullptr;
tobii_device_t* global_device = nullptr;

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

enum tobii_error_t initialize_eye_tracking_api()
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

enum tobii_error_t free_eye_tracking_api()
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

enum tobii_error_t list_eye_tracking_devices(std::vector<std::string>& devices)
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

enum tobii_error_t connect_eye_tracking_device(const std::string& deviceUrl)
{
  if (global_api == nullptr)
    return TOBII_ERROR_NOT_SUBSCRIBED;
  if (global_device != nullptr)
    return TOBII_ERROR_ALREADY_SUBSCRIBED;

  return tobii_device_create(
    global_api, deviceUrl.c_str(), TOBII_FIELD_OF_USE_INTERACTIVE, &global_device);
}

enum tobii_error_t disconnect_eye_tracking_device()
{
  if (global_api == nullptr)
    return TOBII_ERROR_NOT_SUBSCRIBED;
  if (global_device == nullptr)
    return TOBII_ERROR_NOT_SUBSCRIBED;

  const auto result = tobii_device_destroy(global_device);
  global_device = nullptr;
  return result;
}

typedef void (*f_process_gaze_point)(float, float);

enum tobii_error_t connect_eye_tracking_gaze_stream(const f_process_gaze_point handler)
{
  if (global_device == nullptr)
    return TOBII_ERROR_NOT_SUBSCRIBED;

  return  tobii_gaze_point_subscribe(
    global_device,
    [](tobii_gaze_point_t const* gazePoint, void* userData)
    {
      if (gazePoint->validity == TOBII_VALIDITY_VALID)
      {
        // ReSharper disable once CppReinterpretCastFromVoidPtr
        const auto ptr = reinterpret_cast<f_process_gaze_point>(userData);
        ptr(gazePoint->position_xy[0], gazePoint->position_xy[1]);
      }
    },
    // ReSharper disable once CppRedundantCastExpression
    reinterpret_cast<void*>(handler));
}

enum tobii_error_t disconnect_eye_tracking_gaze_stream()
{
  if (global_device == nullptr)
    return TOBII_ERROR_NOT_SUBSCRIBED;

  return tobii_gaze_point_unsubscribe(global_device);
}

/////////////////////////////////////////////////////////////////////////////////////
// ReSharper disable CppInconsistentNaming

JNIEXPORT jstring JNICALL Java_InitializeEyeTrackingApi(JNIEnv* env, jobject)
{
  const auto result = initialize_eye_tracking_api();
  return env->NewStringUTF(present_tobii_error(result));
}

JNIEXPORT jstring JNICALL Java_FreeEyeTrackingApi(JNIEnv* env, jobject)
{
  const auto result = free_eye_tracking_api();
  return env->NewStringUTF(present_tobii_error(result));
}

JNIEXPORT jobjectArray JNICALL Java_ListEyeTrackingDevices(JNIEnv* env, jobject)
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

JNIEXPORT jstring JNICALL Java_ConnectEyeTrackingDevice(JNIEnv* env, jobject, jstring deviceUrl)
{
  const char* native_url = env->GetStringUTFChars(deviceUrl, nullptr);
  const auto device_result = connect_eye_tracking_device(native_url);
  env->ReleaseStringUTFChars(deviceUrl, native_url);

  if (device_result != TOBII_ERROR_NO_ERROR)
    return env->NewStringUTF(present_tobii_error(device_result)); // error

  const auto stream_result =
    connect_eye_tracking_gaze_stream([](float x, float y)
    {
      // TODO: JNI callback
      printf("Gaze point: %f, %f\n", x, y);
    });

  return env->NewStringUTF(present_tobii_error(stream_result));
}

JNIEXPORT jstring JNICALL Java_DisconnectEyeTrackingDevice(JNIEnv* env, jobject)
{
  const auto stream_result = disconnect_eye_tracking_gaze_stream();

  if (stream_result != TOBII_ERROR_NO_ERROR)
    return env->NewStringUTF(present_tobii_error(stream_result)); // error

  const auto device_result = disconnect_eye_tracking_device();
  return env->NewStringUTF(present_tobii_error(device_result));
}

// ReSharper restore CppInconsistentNaming
/////////////////////////////////////////////////////////////////////////////////////


void gaze_point_callback(tobii_gaze_point_t const* gaze_point, void* /* user_data */)
{
  // Check that the data is valid before using it
  if (gaze_point->validity == TOBII_VALIDITY_VALID)
  {
    printf("Gaze point: %f, %f\n",
      gaze_point->position_xy[0],
      gaze_point->position_xy[1]);
  }
}

int main()
{
  auto api_result = initialize_eye_tracking_api();
  std::vector<std::string> devices;
  auto list_result = list_eye_tracking_devices(devices);
  auto connection_result = connect_eye_tracking_device(devices[0]);
  auto stream = connect_eye_tracking_gaze_stream([](float x, float y)
    {
      printf("Gaze point: %f, %f\n", x, y);
    });

  for (int i = 0; i < 1000; i++)
  {
    auto result = tobii_wait_for_callbacks(1, &global_device);

    //auto result = tobii_device_process_callbacks(global_device);

    assert(result == TOBII_ERROR_NO_ERROR || result == TOBII_ERROR_TIMED_OUT);

    // Process callbacks on this thread if data is available
    result = tobii_device_process_callbacks(global_device);
    assert(result == TOBII_ERROR_NO_ERROR);
  }

  disconnect_eye_tracking_gaze_stream();
  disconnect_eye_tracking_device();
  free_eye_tracking_api();
  return 0;


  /*

  // Subscribe to gaze data
  result = tobii_gaze_point_subscribe(device, gaze_point_callback, nullptr);
  assert(result == TOBII_ERROR_NO_ERROR);

  for (int i = 0; i < 1000; i++)
  {
    // Optionally block this thread until data is available.
    // Especially useful if running in a separate thread.
    result = tobii_wait_for_callbacks(1, &device);

    assert(result == TOBII_ERROR_NO_ERROR || result == TOBII_ERROR_TIMED_OUT);

    // Process callbacks on this thread if data is available
    result = tobii_device_process_callbacks(device);
    assert(result == TOBII_ERROR_NO_ERROR);
  }

  // Cleanup
  result = tobii_gaze_point_unsubscribe(device);
  assert(result == TOBII_ERROR_NO_ERROR);
  result = tobii_device_destroy(device);
  assert(result == TOBII_ERROR_NO_ERROR);
  result = tobii_api_destroy(api);
  assert(result == TOBII_ERROR_NO_ERROR);
  return 0;
  */
}