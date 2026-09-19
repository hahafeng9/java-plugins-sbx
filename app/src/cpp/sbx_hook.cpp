#include "sbx_hook.h"

#include <android/log.h>
#include <cstring>

#define LOG_TAG "sbx"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace sbx {

thread_local std::string tls_current_pkg;

static std::string g_last_error;

std::unordered_map<std::string, SandboxEntry>& sandbox_table() {
    static std::unordered_map<std::string, SandboxEntry> table;
    return table;
}

std::mutex& sandbox_mutex() {
    static std::mutex m;
    return m;
}

void set_last_error(const std::string& msg) {
    g_last_error = msg;
    LOGE("%s", msg.c_str());
}

std::string last_error() {
    return g_last_error;
}

bool rewrite_path(const std::string& path, std::string& out) {
    if (tls_current_pkg.empty() || path.empty()) return false;

    std::lock_guard<std::mutex> lk(sandbox_mutex());
    auto it = sandbox_table().find(tls_current_pkg);
    if (it == sandbox_table().end()) return false;

    for (const auto& rule : it->second.rules) {
        if (path.compare(0, rule.src.size(), rule.src) == 0) {
            out = rule.dst + path.substr(rule.src.size());
            return true;
        }
    }
    return false;
}

// ---------------------------------------------------------------------------
// TODO: Replace this stub with your sbx-native hook engine.
//
// libc symbols you typically need to intercept:
//   open, openat, stat, lstat, fstatat, access, faccessat,
//   readlink, readlinkat, rename, renameat, unlink, unlinkat,
//   mkdir, mkdirat, opendir, __openat, __stat
//
// The pattern for each:
//
//   static int (*orig_openat)(int, const char*, int, ...) = nullptr;
//   int my_openat(int dirfd, const char* path, int flags, ...) {
//       std::string rewritten;
//       const char* real = path;
//       if (path && sbx::rewrite_path(path, rewritten)) real = rewritten.c_str();
//       // ensure orig_openat resolved first (via your hook engine)
//       return orig_openat(dirfd, real, flags, ...);
//   }
//
// Install through whatever your sbx-native provides:
//   SHADOWHOOK / Dobby / xHook / PLT hook etc.
// ---------------------------------------------------------------------------
void install_hooks() {
    LOGI("install_hooks(): stub, wire your sbx-native engine here.");
}

} // namespace sbx
