#include "logger-lib.h"
#include "file-util.h"

#define IOException "java/io/IOException"
#define IllegalStateException "java/lang/IllegalStateException"
#define BUFFER_SIZE 1024 * 1024
static char *address = NULL;
static int offset = 0;
static char *buffer = NULL;
static char *old = NULL;
static int count = 0;

char* stringCat(char* first, const char* last) {
    char* cat = (char*) malloc(strlen(first) + strlen(last));
    strcpy(cat, first);
    strcat(cat, last);
    return cat;
}

void throwException(JNIEnv *env, char *message, char* exception) {
    jclass clazz = env->FindClass(exception);
    if (clazz == NULL) return;
    env->ThrowNew(clazz, message);
}

void throwIO(JNIEnv *env, char *message) {
    throwException(env, message, IOException);
}

void throwIllegalState(JNIEnv *env, char *message, ...) {
    throwException(env, message, IllegalStateException);
}

static void startLog(JNIEnv *env, jobject object, jstring path) {
    if (buffer != NULL) {
        throwIllegalState(env, "can not startLog again");
        return;
    }
    const char *_path = env->GetStringUTFChars(path, NULL);
    int result;
    if (file_exist(_path)) {
        result = file_open(_path);
    } else {
        result = file_create_and_open(_path);
    }
    if (result == -1) {
        char* log = stringCat("can not open file : ", _path);
        throwIO(env, log);
        free(log);
        goto final;
    }
    offset = file_make_for_map(_path);
    if (offset == -1) {
        char* log = stringCat("can not make file for map : ", _path);
        throwIO(env, log);
        free(log);
        goto final;
    }
    address = buffer = file_map(result, BUFFER_SIZE, offset);
    count = offset;
    if (buffer == MAP_FAILED) {
        char* log = stringCat("can not map to file : ", _path);
        throwIO(env, log);
        free(log);
        goto final;
    }
    old = file_read(_path);
final:
    env->ReleaseStringUTFChars(path, _path);
}

static void writeLog(JNIEnv *env, jobject object, jstring content) {
    if (buffer == NULL) {
        throwIllegalState(env, "must call the startLog method before call the writeLog");
        return;
    }
    const char *_content = env->GetStringUTFChars(content, NULL);
    size_t size = strlen(_content);
    if (count + size > BUFFER_SIZE) {
        char* log = stringCat("can not write log : ", _content);
        throwIO(env, log);
        free(log);
        goto final;
    }
    memcpy(buffer, _content, size);
    buffer += size;
    count += size;
final:
    env->ReleaseStringUTFChars(content, _content);
}

static jstring readLog(JNIEnv *env, jobject object) {
    if (buffer == NULL) {
        throwIllegalState(env, "must call the startLog method before call the readLog");
        return NULL;
    }
    if (old == NULL) {
        return env->NewStringUTF(address);
    } else {
        const char *buffers = address;
        char *log = stringCat(old, buffers);
        return env->NewStringUTF(log);
    }
}

static void stopLog(JNIEnv *env, jobject object) {
    if (buffer == NULL) {
        throwIllegalState(env, "must call the startLog method before call the stopLog");
        return;
    }
    file_unmap(address, BUFFER_SIZE - offset);
    address = NULL;
    offset = 0;
    buffer = NULL;
    free(old);
    count = 0;
}

#define LOGGER "com/johan/android/logger/Logger"

static JNINativeMethod nativeMethods[] = {
        {"startLog", "(Ljava/lang/String;)V", (void*) startLog},
        {"writeLog", "(Ljava/lang/String;)V", (void*) writeLog},
        {"readLog", "()Ljava/lang/String;", (void*) readLog},
        {"stopLog", "()V", (void*) stopLog}
};

static jint registerNativeMethod(JNIEnv *env) {
    jclass clazz = env->FindClass(LOGGER);
    if (clazz == NULL) {
        return 0;
    }
    if (env->RegisterNatives(clazz, nativeMethods, sizeof(nativeMethods) / sizeof(nativeMethods[0])) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

jint JNI_OnLoad(JavaVM *vm, void* reserved) {
    JNIEnv *env;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    if (!registerNativeMethod(env)) {
        return -1;
    }
    return JNI_VERSION_1_4;
}



