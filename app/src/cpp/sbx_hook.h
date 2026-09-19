#pragma once
#include <jni.h>
#include <string>
#include <unordered_map>
#include <vector>
#include <mutex>

namespace sbx {

struct PathRule {
    std::string src;   // e.g. /data/data/com.demo.plugin
    std::string dst;   // e.g. /data/data/host/files/sbx/com.demo.plugin/data
    int mode;          // 0 = prefix rewrite
};

struct SandboxEntry {
    std::string pkg;
    std::vector<PathRule> rules;
    int uid = -1;
};

// pkg -> entry
std::unordered_map<std::string, SandboxEntry>& sandbox_table();
std::mutex& sandbox_mutex();

// thread-local current plugin; empty => not in plugin context
extern thread_local std::string tls_current_pkg;

// Install the redirection hooks. TODO: call into sbx-native's hook engine.
void install_hooks();

// Rewrite `path` according to current thread's plugin rules.
// Returns true if rewritten (result in `out`).
bool rewrite_path(const std::string& path, std::string& out);

void set_last_error(const std::string& msg);
std::string last_error();

} // namespace sbx
