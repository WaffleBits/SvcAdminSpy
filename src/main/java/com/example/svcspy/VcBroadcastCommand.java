package com.example.svcspy;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class VcBroadcastCommand implements CommandExecutor {

    private final AdminSpyVoicePlugin voicePlugin;
    private final Plugin bukkitPlugin;

    public VcBroadcastCommand(AdminSpyVoicePlugin voicePlugin, Plugin bukkitPlugin) {
        this.voicePlugin = voicePlugin;
        this.bukkitPlugin = bukkitPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("svcspy.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        UUID id = player.getUniqueId();
        boolean enabled = voicePlugin.toggleBroadcast(id);
        if (enabled) {
            player.sendMessage(ChatColor.GREEN + "[SVC] Broadcast mode ENABLED. You broadcast globally when you speak.");
        } else {
            player.sendMessage(ChatColor.YELLOW + "[SVC] Broadcast mode DISABLED.");
        }

        bukkitPlugin.getLogger().info("[SvcAdminSpy] " + player.getName() + " set broadcast mode to " + enabled);
        return true;
    }
}
