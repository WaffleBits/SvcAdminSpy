package com.example.svcspy;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdminSpyBukkitPlugin extends JavaPlugin {

    private AdminSpyVoicePlugin voicePlugin;

    @Override
    public void onEnable() {
        // Hook into Simple Voice Chat's Bukkit service
        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service == null) {
            getLogger().severe("Simple Voice Chat not found – disabling SvcAdminSpy.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Create and register our voicechat plugin
        voicePlugin = new AdminSpyVoicePlugin(this, service);
        service.registerPlugin(voicePlugin);

        // Register /vcspy command
        PluginCommand cmd = getCommand("vcspy");
        if (cmd != null) {
            cmd.setExecutor(new VcSpyCommand(voicePlugin, this));
        } else {
            getLogger().severe("Command vcspy not defined in plugin.yml");
        }

        // Register /vcbroadcast command
        PluginCommand bcmd = getCommand("vcbroadcast");
        if (bcmd != null) {
            bcmd.setExecutor(new VcBroadcastCommand(voicePlugin, this));
        } else {
            getLogger().severe("Command vcbroadcast not defined in plugin.yml");
        }

        getLogger().info("SvcAdminSpy enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("SvcAdminSpy disabled.");
    }
}
