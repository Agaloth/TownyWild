package com.agaloth.townywild.tasks;

import com.agaloth.townywild.listeners.TownyWildTownEventListener;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.agaloth.townywild.TownyWild.plugin;
import static com.agaloth.townywild.settings.Settings.getConfig;

public class UpdateBossBarPlaceholder implements Runnable {
    private final UUID uuid;
    private final long scheduledRemovalTime;
    private final BossBar bossBar;
    private final TownyWildTownEventListener listener;
    private int taskId;

    public UpdateBossBarPlaceholder(UUID uuid, long scheduledRemovalTime, BossBar bossBar, TownyWildTownEventListener listener) {
        this.uuid = uuid;
        this.scheduledRemovalTime = scheduledRemovalTime;
        this.bossBar = bossBar;
        this.listener = listener;
    }

    @Override
    public void run() {
    }

    public void startProgressUpdater() {
    }
}

