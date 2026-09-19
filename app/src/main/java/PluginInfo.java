package com.sbx.plugins;

import androidx.annotation.Nullable;

public class PluginInfo {
    public final String pkg;
    public final String apkPath;
    @Nullable public final String nativeLibPath;
    @Nullable public final String entryClass;

    public PluginInfo(String pkg, String apkPath,
                      @Nullable String nativeLibPath,
                      @Nullable String entryClass) {
        this.pkg = pkg;
        this.apkPath = apkPath;
        this.nativeLibPath = nativeLibPath;
        this.entryClass = entryClass;
    }
}
