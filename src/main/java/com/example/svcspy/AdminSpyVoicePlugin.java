package com.example.svcspy;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AdminSpyVoicePlugin implements VoicechatPlugin {

    private final Plugin bukkitPlugin;
    private final BukkitVoicechatService voicechatService;
    private volatile VoicechatServerApi serverApi;

    // UUIDs of admins currently in spy (hear-only) and broadcast modes
    private final Set<UUID> spyHearAdmins = ConcurrentHashMap.newKeySet();
    private final Set<UUID> broadcastAdmins = ConcurrentHashMap.newKeySet();

    public AdminSpyVoicePlugin(Plugin bukkitPlugin, BukkitVoicechatService service) {
        this.bukkitPlugin = bukkitPlugin;
        this.voicechatService = service;
    }

    @Override
    public String getPluginId() {
        return "svc_admin_spy";
    }

    @Override
    public void initialize(VoicechatApi api) {
        if (api instanceof VoicechatServerApi serverApi) {
            this.serverApi = serverApi;
            bukkitPlugin.getLogger().info("[SvcAdminSpy] VoicechatServerApi initialized");
        } else {
            bukkitPlugin.getLogger().severe("[SvcAdminSpy] Received non-server VoicechatApi!");
        }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
        registration.registerEvent(PlayerDisconnectedEvent.class, this::onPlayerDisconnected);
    }

    // === Public API for the Bukkit command ===

    public boolean toggleSpy(UUID playerId) {
        boolean enabled;
        if (spyHearAdmins.contains(playerId)) {
            spyHearAdmins.remove(playerId);
            enabled = false;
        } else {
            spyHearAdmins.add(playerId);
            enabled = true;
        }
        return enabled;
    }

    public boolean toggleBroadcast(UUID playerId) {
        boolean enabled;
        if (broadcastAdmins.contains(playerId)) {
            broadcastAdmins.remove(playerId);
            enabled = false;
        } else {
            broadcastAdmins.add(playerId);
            enabled = true;
        }
        return enabled;
    }

    public boolean isSpy(UUID playerId) {
        return spyHearAdmins.contains(playerId);
    }

    public boolean isBroadcaster(UUID playerId) {
        return broadcastAdmins.contains(playerId);
    }

    // === Voice Chat event handlers ===

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatServerApi api = serverApi;
        if (api == null) {
            return;
        }

        VoicechatConnection senderConn = event.getSenderConnection();
        if (senderConn == null || senderConn.getPlayer() == null) {
            return;
        }

        UUID senderId = senderConn.getPlayer().getUuid();
        MicrophonePacket micPacket = event.getPacket();
        if (micPacket == null) {
            return;
        }

        // Convert player's mic packet to a non-directional static packet
        StaticSoundPacket staticPacket = micPacket.toStaticSoundPacket();

        // CASE 1: Admin speaking – if in broadcast mode, broadcast globally
        if (broadcastAdmins.contains(senderId)) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                try {
                    var targetConn = api.getConnectionOf(online.getUniqueId());
                    if (targetConn == null || targetConn.isDisabled()) continue;
                    if (online.getUniqueId().equals(senderId)) continue; // avoid echo to self
                    api.sendStaticSoundPacketTo(targetConn, staticPacket);
                } catch (Exception ignored) {
                }
            }
            event.cancel();
            return;
        }

        // CASE 2: Normal player speaking – forward to each spy admin
        if (spyHearAdmins.isEmpty()) {
            return;
        }

        for (UUID adminId : spyHearAdmins) {
            try {
                var adminPlayer = Bukkit.getPlayer(adminId);
                if (adminPlayer == null) continue;
                var adminConn = api.getConnectionOf(adminPlayer.getUniqueId());
                if (adminConn == null || adminConn.isDisabled()) continue;
                api.sendStaticSoundPacketTo(adminConn, staticPacket);
            } catch (Exception ignored) {
            }
        }
    }

    private void onPlayerDisconnected(PlayerDisconnectedEvent event) {
        UUID playerId = event.getPlayerUuid();
        spyHearAdmins.remove(playerId);
        broadcastAdmins.remove(playerId);
    }
}
