package com.sbx.plugins.sandbox;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.File;

public class SandboxedContext extends ContextWrapper {

    private final PluginSandbox sandbox;
    private Resources resources;
    private AssetManager assets;

    public SandboxedContext(Context base, PluginSandbox sandbox) {
        super(base);
        this.sandbox = sandbox;
    }

    public void setResources(Resources r) { this.resources = r; }
    public void setAssets(AssetManager a) { this.assets = a; }

    @Override public String getPackageName() { return sandbox.pkg; }

    @Override public File getFilesDir()                { return sandbox.filesDir; }
    @Override public File getCacheDir()                { return sandbox.cacheDir; }
    @Override public File getNoBackupFilesDir()        { return sandbox.filesDir; }
    @Override public File getDatabasePath(String name) { return new File(sandbox.databasesDir, name); }

    @Override public File getExternalFilesDir(String type) {
        return type == null ? sandbox.externalDataDir : new File(sandbox.externalDataDir, type);
    }
    @Override public File getExternalCacheDir() {
        return new File(sandbox.externalDataDir, "cache");
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return getBaseContext().getSharedPreferences(
                "sbx_" + sandbox.pkg + "_" + name, mode);
    }

    @Override public Resources getResources() {
        return resources != null ? resources : super.getResources();
    }
    @Override public AssetManager getAssets() {
        return assets != null ? assets : super.getAssets();
    }
    @Override public ClassLoader getClassLoader() {
        return sandbox.classLoader != null ? sandbox.classLoader : super.getClassLoader();
    }

    @Override
    public Context createPackageContext(String packageName, int flags)
            throws PackageManager.NameNotFoundException {
        if (sandbox.pkg.equals(packageName)) return this;
        return super.createPackageContext(packageName, flags);
    }

    @Override public void startActivity(Intent intent)                      { super.startActivity(rewrite(intent)); }
    @Override public ComponentName startService(Intent service)             { return super.startService(rewrite(service)); }
    @Override public boolean bindService(Intent s, ServiceConnection c, int f){ return super.bindService(rewrite(s), c, f); }

    private Intent rewrite(Intent intent) {
        if (intent == null) return null;
        Intent copy = new Intent(intent);
        copy.putExtra("sbx.plugin.pkg", sandbox.pkg);
        return copy;
    }
}
