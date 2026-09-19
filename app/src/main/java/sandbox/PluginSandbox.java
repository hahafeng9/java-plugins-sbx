package com.sbx.plugins.sandbox;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.File;
import java.lang.reflect.Method;

public final class PluginSandbox {

    public final String pkg;
    public final int uid;
    public final File root;
    public final File dataDir;
    public final File filesDir;
    public final File cacheDir;
    public final File databasesDir;
    public final File sharedPrefsDir;
    public final File externalDataDir;

    public SandboxedPluginClassLoader classLoader;
    public SandboxedContext context;

    public PluginSandbox(String pkg, File root, int uid) {
        this.pkg = pkg;
        this.uid = uid;
        this.root = root;
        this.dataDir        = new File(root, "data");
        this.filesDir       = new File(dataDir, "files");
        this.cacheDir       = new File(dataDir, "cache");
        this.databasesDir   = new File(dataDir, "databases");
        this.sharedPrefsDir = new File(dataDir, "shared_prefs");
        this.externalDataDir = new File(root, "external");
    }

    public void prepare() {
        mkdirs(filesDir, cacheDir, databasesDir, sharedPrefsDir, externalDataDir);
    }

    public void install(Context host, String apkPath, String nativeLibPath) throws Exception {
        File dexOpt = new File(root, "dexopt");
        mkdirs(dexOpt);

        classLoader = new SandboxedPluginClassLoader(
                apkPath,
                dexOpt.getAbsolutePath(),
                nativeLibPath,
                host.getClassLoader());

        context = new SandboxedContext(host, this);
        Resources res = buildResources(host, apkPath);
        context.setResources(res);
        context.setAssets(res.getAssets());
    }

    private static Resources buildResources(Context host, String apkPath) throws Exception {
        AssetManager am = AssetManager.class.getDeclaredConstructor().newInstance();
        Method addAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
        addAssetPath.setAccessible(true);
        addAssetPath.invoke(am, apkPath);

        return new Resources(
                am,
                host.getResources().getDisplayMetrics(),
                host.getResources().getConfiguration());
    }

    private static void mkdirs(File... dirs) {
        for (File d : dirs) {
            if (!d.exists() && !d.mkdirs()) {
                throw new IllegalStateException("mkdir failed: " + d);
            }
        }
    }
}
