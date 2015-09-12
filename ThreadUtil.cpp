#include <jni.h>
#include <iostream>
#include <unistd.h>
#include "ThreadUtil.h"
using namespace std;

JNIEXPORT void JNICALL Java_alt_collections_util_JniThreadUtil_usleep (JNIEnv *, jclass, jlong microseconds) {
    usleep(microseconds);
    return;
}
