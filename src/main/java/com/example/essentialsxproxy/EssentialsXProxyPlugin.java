package com.example.essentialsxproxy;

import net.ess3.api.IEssentials;
import org.bukkit.plugin.java.JavaPlugin;

public final class EssentialsXProxyPlugin extends JavaPlugin {

    private IEssentials essentials;

    @Override
    public void onEnable() {
        essentials = (IEssentials) getServer().getPluginManager().getPlugin("Essentials");
        if (essentials == null) {
            getLogger().severe("EssentialsX not found.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("EssentialsXProxy enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("EssentialsXProxy disabled.");
    }
}
