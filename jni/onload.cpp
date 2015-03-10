#include <string.h>
#include <assert.h>
#include "jni.h"


namespace example {
    int register_Signal(JNIEnv* env);
    int register_Signal2(JNIEnv *env);
};

using namespace example;

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }
    assert(env != NULL);
    if (!register_Signal(env)) {
        return result;
    }
    if (!register_Signal2(env)) {
            return result;
        }
    /* success -- return valid version number */
    return JNI_VERSION_1_4;
}
