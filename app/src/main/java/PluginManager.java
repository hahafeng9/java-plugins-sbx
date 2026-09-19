package com.sbx.plugins;

import android.content.Context;

import com.sbx.plugins.sandbox.PluginSandbox;
import com.sbx.plugins.sandbox.SbxSandboxManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PluginManager {

    private static final PluginManager INSTANCE = new PluginManager();
    private final Map<String, PluginInfo> installed = new ConcurrentHashMap<>();
    private Context host;
    private boolean inited;

    private PluginManager() {}

    public static PluginManager get() { return INSTANCE; }

    public synchronized void init(Context context) {
        if (inited) return;
        host = context.getApplicationContext();
        SbxSandboxManager.get().initHost(host);
        inited = true;
    }

    public synchronized void install(PluginInfo info) throws Exception {
        checkInit();
        PluginSandbox sandbox = SbxSandboxManager.get().getOrCreate(info.pkg);
        sandbox.install(host, info.apkPath, info.nativeLibPath);
        installed.put(info.pkg, info);
    }

    public void launch(String pkg) {
        checkInit();
        PluginInfo info = installed.get(pkg);
        if (info == null) throw new IllegalStateException("plugin not installed: " + pkg);
        if (info.entryClass == null) return;

        PluginSandbox sandbox = SbxSandboxManager.get().get(pkg);
        SbxSandboxManager.get().enter(sandbox);
        try {
            Class<?> entry = sandbox.classLoader.loadClass(info.entryClass);
            Object obj = entry.getDeclaredConstructor().newInstance();
            entry.getMethod("onCreate", Context.class).invoke(obj, sandbox.context);
        } catch (Throwable t) {
            throw new RuntimeException("launch failed: " + pkg, t);
        } finally {
            SbxSandboxManager.get().exit();
        }
    }

    private void checkInit() {
        if (!inited) throw new IllegalStateException("PluginManager not initialised");
    }
}
