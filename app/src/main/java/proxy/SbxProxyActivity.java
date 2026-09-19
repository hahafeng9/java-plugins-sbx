package com.sbx.plugins.proxy;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.sbx.plugins.sandbox.PluginSandbox;
import com.sbx.plugins.sandbox.SbxSandboxManager;

import java.lang.reflect.Method;

public class SbxProxyActivity extends Activity {

    public static final String EXTRA_PKG = "sbx.plugin.pkg";
    public static final String EXTRA_CLS = "sbx.plugin.activity";

    private PluginSandbox sandbox;
    private Object delegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String pkg = getIntent().getStringExtra(EXTRA_PKG);
        String cls = getIntent().getStringExtra(EXTRA_CLS);

        sandbox = SbxSandboxManager.get().getOrCreate(pkg);
        SbxSandboxManager.get().enter(sandbox);
        try {
            Class<?> c = sandbox.classLoader.loadClass(cls);
            delegate = c.getDeclaredConstructor().newInstance();

            invoke(delegate, "attach", new Class<?>[]{ Activity.class, Context.class },
                    new Object[]{ this, sandbox.context });

            invoke(delegate, "onCreate", new Class<?>[]{ Bundle.class },
                    new Object[]{ savedInstanceState });
        } catch (Throwable t) {
            throw new RuntimeException("ProxyActivity.onCreate failed", t);
        } finally {
            SbxSandboxManager.get().exit();
        }
    }

    @Override protected void onResume()  { pass("onResume"); super.onResume(); }
    @Override protected void onPause()   { pass("onPause");  super.onPause(); }
    @Override protected void onStop()    { pass("onStop");   super.onStop(); }
    @Override protected void onDestroy() { pass("onDestroy");super.onDestroy(); }

    private void pass(String name) {
        if (delegate == null) return;
        SbxSandboxManager.get().enter(sandbox);
        try {
            invoke(delegate, name, null, null);
        } catch (Throwable t) {
            throw new RuntimeException("ProxyActivity." + name + " failed", t);
        } finally {
            SbxSandboxManager.get().exit();
        }
    }

    private static Object invoke(Object target, String method,
                                 Class<?>[] types, Object[] args) throws Exception {
        Method m = target.getClass().getMethod(method, types);
        m.setAccessible(true);
        return m.invoke(target, args);
    }
}
