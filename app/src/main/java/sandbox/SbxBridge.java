package com.sbx.plugins.sandbox;

/**
 * JNI bridge to libsbx.so.
 * Native method names must match exported symbols in app/src/main/cpp/sbx_bridge.cpp.
 *
 * If your local sbx-native uses different names, rename here and in the .cpp.
 */
public final class SbxBridge {

    private static volatile boolean loaded;

    private SbxBridge() {}

    public static synchronized void ensureLoaded() {
        if (loaded) return;
        System.loadLibrary("sbx");
        loaded = true;
    }

    /** Initialise host context. Returns 0 on success. */
    public static native int init(String hostPkg, String sandboxRoot, int uid);

    /**
     * Add a path redirection rule.
     * mode: 0 = exact prefix rewrite, 1 = transparent passthrough.
     */
    public static native int addPathRule(String pkg, String src, String dst, int mode);

    /** Mark current thread as executing plugin `pkg`. */
    public static native int enter(String pkg, int uid);

    /** Clear current thread's plugin context. */
    public static native int exit();

    /** Last error string set by native side. */
    public static native String lastError();
}
