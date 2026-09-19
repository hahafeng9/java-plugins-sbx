package com.sbx.plugins.sandbox;

import android.content.Context;
import android.os.Process;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SbxSandboxManager {

    private static final SbxSandboxManager INSTANCE = new SbxSandboxManager();

    private final Map<String, PluginSandbox> sandboxes = new ConcurrentHashMap<>();
    private File root;
    private boolean inited;

    private SbxSandboxManager() {}

    public static SbxSandboxManager get() {
        return INSTANCE;
    }

    public synchronized void initHost(Context host) {
        if (inited) return;
        SbxBridge.ensureLoaded();

        root = new File(host.getFilesDir(), "sbx");
        if (!root.exists() && !root.mkdirs()) {
            throw new IllegalStateException("create sandbox root failed: " + root);
        }

        int r = SbxBridge.init(host.getPackageName(), root.getAbsolutePath(), Process.myUid());
        if (r != 0) {
            throw new IllegalStateException("sbx init failed: " + SbxBridge.lastError());
        }
        inited = true;
    }

    public PluginSandbox getOrCreate(String pkg) {
        return sandboxes.computeIfAbsent(pkg, p -> {
            PluginSandbox s = new PluginSandbox(p, new File(root, p), Process.myUid());
            s.prepare();
            addRules(s);
            return s;
        });
    }

    public PluginSandbox get(String pkg) {
        return sandboxes.get(pkg);
    }

    private void addRules(PluginSandbox s) {
        SbxBridge.addPathRule(s.pkg, "/data/data/" + s.pkg,   s.dataDir.getAbsolutePath(), 0);
        SbxBridge.addPathRule(s.pkg, "/data/user/0/" + s.pkg, s.dataDir.getAbsolutePath(), 0);
        SbxBridge.addPathRule(s.pkg,
                "/storage/emulated/0/Android/data/" + s.pkg,
                s.externalDataDir.getAbsolutePath(), 0);
    }

    public void enter(PluginSandbox s) {
        if (s == null) return;
        SbxBridge.enter(s.pkg, s.uid);
    }

    public void exit() {
        SbxBridge.exit();
    }
}
