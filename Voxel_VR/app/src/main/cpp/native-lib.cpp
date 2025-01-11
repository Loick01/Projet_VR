#include <jni.h>
#include <string>
#include <GLES3/gl3.h>

#define LOG_TAG "OpenGL_ES"

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_voxel_1vr_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_voxel_1vr_MainActivity_glInit(JNIEnv* env, jobject obj) {

    glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

}