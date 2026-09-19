package com.sbx.plugins;

import android.app.Application;
import android.content.Context;

public class PluginHostApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        PluginManager.get().init(base);
    }
}
