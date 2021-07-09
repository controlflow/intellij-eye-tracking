// ConsoleApplication1.cpp : This file contains the 'main' function. Program execution begins and ends there.
//

#include <vector>
#include <cassert>
#include <iostream>
#include "include/jni.h"
#include "include/tobii/tobii.h"
#include "include/tobii/tobii_streams.h"



tobii_api_t* global_api = nullptr;

std::string present_tobii_error(const tobii_error_t error)
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
      return "ERROR_OPERATION_FAILED";
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

std::string initialize_eye_tracking_api()
{
  if (global_api != nullptr)
    return "ERROR_ALREADY_INITIALIZED";

  tobii_api_t* api = nullptr;
  const auto result = tobii_api_create(&api, nullptr, nullptr);
  if (result != TOBII_ERROR_NO_ERROR)
    return present_tobii_error(result);

  global_api = api;
  return "";
}

std::string free_eye_tracking_api()
{
  if (global_api == nullptr)
    return "ERROR_NOT_INITIALIZED";

  const auto result = tobii_api_destroy(global_api);
  if (result != TOBII_ERROR_NO_ERROR)
    return present_tobii_error(result);

  global_api = nullptr;
  return "";
}

std::vector<std::string> get_eye_tracking_devices()
{
  std::vector<std::string> devices;

  if (global_api == nullptr)
  {
    devices.emplace_back("ERROR_NOT_INITIALIZED");
  }
  else
  {
    const auto result = tobii_enumerate_local_device_urls(
      global_api,
      [](char const* url, void* userData)
      {
        auto* devices_ptr = static_cast<std::vector<std::string>*>(userData);
        devices_ptr->push_back(std::string(url));
      },
      &devices);

    devices.push_back(present_tobii_error(result));
  }

  return devices;
}

/////////////////////////////////////////////////////////////////////////////////////

JNIEXPORT jstring JNICALL Java_InitializeEyeTrackingApi(JNIEnv* env, jobject)
{
  const auto result = initialize_eye_tracking_api();
  return env->NewStringUTF(result.c_str());
}

JNIEXPORT jstring JNICALL Java_FreeEyeTrackingApi(JNIEnv* env, jobject)
{
  const auto result = free_eye_tracking_api();
  return env->NewStringUTF(result.c_str());
}

JNIEXPORT jobjectArray JNICALL Java_GetEyeTrackingDevices(JNIEnv* env, jobject)
{
  const auto devices = get_eye_tracking_devices();

  const auto array_object = env->NewObjectArray(
    static_cast<jsize>(devices.size()), env->FindClass("java/lang/String"), nullptr);

  if (array_object == nullptr) return nullptr;

  auto index = 0;
  for (auto const& element : devices)
  {
    const auto str = env->NewStringUTF(element.c_str());
    env->SetObjectArrayElement(array_object, index++, str);
  }

  return array_object;
}

JNIEXPORT void JNICALL Java_HelloJNI_sayHello(JNIEnv* env, jobject thisObj)
{
  printf("Hello World From Native!\n");
  return;
}

/////////////////////////////////////////////////////////////////////////////////////


void url_receiver(char const* url, void* user_data)
{
  const auto buffer = static_cast<char*>(user_data);
  if (*buffer != '\0')
    return; // only keep first value

  if (strlen(url) < 256)
    strcpy_s(buffer, 256, url);
}

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
  auto devices = get_eye_tracking_devices();

  return 0;

  tobii_api_t* api = global_api;
  //tobii_error_t result = tobii_api_create(&api, nullptr, nullptr);
  //assert(result == TOBII_ERROR_NO_ERROR);

  char url[256] = { 0 };
  //result = tobii_enumerate_local_device_urls(api, url_receiver, url);

  tobii_error_t result;
  result = tobii_enumerate_local_device_urls(global_api, url_receiver, url);


  assert(result == TOBII_ERROR_NO_ERROR);
  if (*url == '\0')
  {
    printf("Error: No device found\n");
    return 1;
  }

  // Connect to the first tracker found
  tobii_device_t* device = nullptr;
  result = tobii_device_create(api, url, TOBII_FIELD_OF_USE_INTERACTIVE, &device);
  assert(result == TOBII_ERROR_NO_ERROR);

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
}