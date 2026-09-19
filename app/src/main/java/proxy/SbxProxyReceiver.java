package com.sbx.plugins.proxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sbx.plugins.sandbox.PluginSandbox;
import com.sbx.plugins.sandbox.SbxSandboxManager;

public class SbxProxyReceiver extends BroadcastReceiver {

    public static final String EXTRA_PKG = "sbx.plugin.pkg";
    public static final String EXTRA_CLS = "sbx.plugin.receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String pkg = intent.getStringExtra(EXTRA_PKG);
        String cls = intent.getStringExtra(EXTRA_CLS);
        if (pkg == null || cls == null) return;

        PluginSandbox sandbox = SbxSandboxManager.get().getOrCreate(pkg);
        SbxSandboxManager.get().enter(sandbox);
        try {
            Class<?> c = sandbox.classLoader.loadClass(cls);
            Object delegate = c.getDeclaredConstructor().newInstance();
            c.getMethod("onReceive", Context.class, Intent.class)
                    .invoke(delegate, sandbox.context, intent);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            SbxSandboxManager.get().exit();
        }
    }
}
