LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional \
#########    tests

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
	src/com/goodocom/gocsdk/IGocsdkService.aidl \
	src/com/goodocom/gocsdk/IGocsdkCallback.aidl

LOCAL_PACKAGE_NAME := BtPhone

#LOCAL_SDK_VERSION := current

LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_FLAG_FILES :=

include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
