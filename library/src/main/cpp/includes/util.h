//
// Created by Administrator on 2019/5/17.
//

#ifndef LOGGER_UTIL_H
#define LOGGER_UTIL_H

#include <android/log.h>
#define TAG "LOGGER"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#endif //LOGGER_UTIL_H
