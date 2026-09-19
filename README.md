# java-plugins-sbx

Single-process Android plugin host with native sandbox path redirection.

## Features
- Single-process plugin sandbox (no `:plugin` process)
- `SandboxedPluginClassLoader` (parent-last for plugin classes)
- `SandboxedContext` (files/cache/db/prefs redirected per plugin)
- Proxy Activity / Service / Receiver hosted in the host process
- Native bridge `libsbx.so` with thread-local current-plugin redirection

## Layer
```
Host process
├── Host Application
├── libsbx.so (JNI)
│   └── thread-local currentPlugin
└── PluginSandbox(pkg)
    ├── SandboxedPluginClassLoader
    ├── SandboxedContext
    └── Proxy components
```

## Integrate with your `sbx-native`
Replace the TODOs in `app/src/main/cpp/sbx_hook.cpp` with your hook engine
(ShadowHook / Dobby / xHook / etc). Keep:

1. `thread_local std::string g_current_pkg`
2. Path-rewrite table keyed by `pkg`
3. `enter(pkg)` / `exit()` set/clear the thread-local

## Usage
```java
PluginManager pm = PluginManager.get();
pm.init(context);
pm.install(new PluginInfo("com.demo.plugin", "/sdcard/plugin.apk", null));
pm.launch("com.demo.plugin");
```

## License
MIT