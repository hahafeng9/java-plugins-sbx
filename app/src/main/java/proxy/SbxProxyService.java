package com.sbx.plugins.proxy;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.sbx.plugins.sandbox.PluginSandbox;
import com.sbx.plugins.sandbox.SbxSandboxManager;

import java.lang.reflect.Method;

public class SbxProxyService extends Service {

    public static final String EXTRA_PKG = "sbx.plugin.pkg";
    public static final String EXTRA_CLS = "sbx.plugin.service";

    private PluginSandbox sandbox;
    private Object delegate;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && delegate == null) {
            String pkg = intent.getStringExtra(EXTRA_PKG);
            String cls = intent.getStringExtra(EXTRA_CLS);
            if (pkg != null && cls != null) {
                sandbox = SbxSandboxManager.get().getOrCreate(pkg);
                SbxSandboxManager.get().enter(sandbox);
                try {
                    Class<?> c = sandbox.classLoader.loadClass(cls);
                    delegate = c.getDeclaredConstructor().newInstance();
                    Method attach = c.getMethod("attach", android.content.Context.class, Service.class);
                    attach.invoke(delegate, sandbox.context, this);
                    c.getMethod("onCreate").invoke(delegate);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                } finally {
                    SbxSandboxManager.get().exit();
                }
            }
        }
        if (delegate != null) {
            SbxSandboxManager.get().enter(sandbox);
            try {
                delegate.getClass().getMethod("onStartCommand", Intent.class, int.class, int.class)
                        .invoke(delegate, intent, flags, startId);
            } catch (Throwable ignored) {
            } finally {
                SbxSandboxManager.get().exit();
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        if (delegate != null) {
            SbxSandboxManager.get().enter(sandbox);
            try {
                delegate.getClass().getMethod("onDestroy").invoke(delegate);
            } catch (Throwable ignored) {
            } finally {
                SbxSandboxManager.get().exit();
            }
        }
        super.onDestroy();
    }
}
