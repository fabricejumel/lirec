LOCAL_PATH             := $(call my-dir)

include $(CLEAR_VARS)

NDK_DEBUG_IMPORTS      := 1
NDK_DEBUG_MODULES      := 1
LOCAL_CFLAGS           := -O2 -DANDROID_NDK
LOCAL_MODULE           := libS3DClient
LOCAL_SRC_FILES        := S3DClient.cpp
LOCAL_LDLIBS           := $(APP_PROJECT_LIBPATH)/obj/local/$(TARGET_ARCH_ABI)/libS3DClient_Android.a
LOCAL_LDLIBS           += -L$(APP_PROJECT_LIBPATH)/obj/local/$(TARGET_ARCH_ABI) -lopenal
LOCAL_LDLIBS           += -lGLESv1_CM -ldl -llog
include $(BUILD_SHARED_LIBRARY)
