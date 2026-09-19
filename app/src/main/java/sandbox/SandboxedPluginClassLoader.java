package com.sbx.plugins.sandbox;

import dalvik.system.DexClassLoader;

public class SandboxedPluginClassLoader extends DexClassLoader {

    private static final String[] PARENT_FIRST_PREFIXES = {
            "android.", "androidx.", "com.google.android.",
            "java.", "javax.", "sun.", "kotlin.", "kotlinx.",
            "org.jetbrains.", "org.json.", "org.w3c.", "org.xml."
    };

    public SandboxedPluginClassLoader(String dexPath,
                                      String optimizedDirectory,
                                      String librarySearchPath,
                                      ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (isParentFirst(name)) {
            return super.loadClass(name, resolve);
        }
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    c = findClass(name);
                } catch (ClassNotFoundException e) {
                    c = getParent().loadClass(name);
                }
            }
            if (resolve) resolveClass(c);
            return c;
        }
    }

    private static boolean isParentFirst(String name) {
        for (String prefix : PARENT_FIRST_PREFIXES) {
            if (name.startsWith(prefix)) return true;
        }
        return false;
    }
}
