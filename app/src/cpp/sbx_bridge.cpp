#include "sbx_hook.h"

#include <android/log.h>
#include <string>

#define LOG_TAG "sbx"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)

using namespace sbx;

static std::string jstr(JNIEnv* env, jstring s) {
    if (!s) return {};
    const char* c = env->GetStringUTFChars(s, nullptr);
    std::string r = c ? c : "";
    if (c) env->ReleaseStringUTFChars(s, c);
    return r;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_sbx_plugins_sandbox_SbxBridge_init(
        JNIEnv* env, jclass, jstring hostPkg, jstring sandboxRoot, jint uid) {
    std::string host = jstr(env, hostPkg);
    std::string root = jstr(env, sandboxRoot);
    install_hooks();
    LOGI("init host=%s root=%s uid=%d", host.c_str(), root.c_str(), uid);
    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_sbx_plugins_sandbox_SbxBridge_addPathRule(
        JNIEnv* env, jclass, jstring pkg, jstring src, jstring dst, jint mode) {
    std::string p = jstr(env, pkg);
    if (p.empty()) {
        set_last_error("addPathRule: empty pkg");
        return -1;
    }
    std::lock_guard<std::mutex> lk(sandbox_mutex());
    SandboxEntry& e = sandbox_table()[p];
    e.pkg = p;
    e.rules.push_back({ jstr(env, src), jstr(env, dst), (int)mode });
    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_sbx_plugins_sandbox_SbxBridge_enter(
        JNIEnv* env, jclass, jstring pkg, jint uid) {
    tls_current_pkg = jstr(env, pkg);
    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_sbx_plugins_sandbox_SbxBridge_exit(JNIEnv*, jclass) {
    tls_current_pkg.clear();
    return 0;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sbx_plugins_sandbox_SbxBridge_lastError(JNIEnv* env, jclass) {
    return env->NewStringUTF(sbx::last_error().c_str());
}
