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
    public int getTaskId() {
        return taskId;
    }

    public UpdateBossBarPlaceholder(UUID uuid, long scheduledRemovalTime, BossBar bossBar, TownyWildTownEventListener listener) {
        this.uuid = uuid;
        this.scheduledRemovalTime = scheduledRemovalTime;
        this.bossBar = bossBar;
        this.listener = listener;
    }

    @Override
    public void run() {
        long remainingProtectionTime = scheduledRemovalTime;
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            listener.remove(uuid);
            Bukkit.getScheduler().cancelTask(taskId);
            return;
        }
        String bossBarText = PlaceholderAPI.setPlaceholders(player, getConfig().getString("bossbar_message", "You are protected from PVP for %townywild_countdown%!"));
        bossBarText = ChatColor.translateAlternateColorCodes('&', bossBarText);
        double timeDecrease =  (double) 0.1 / remainingProtectionTime;
        double progress = ((double) scheduledRemovalTime);
        System.out.println(progress + "%");
        if (progress < 0) {
            progress = 0;
        } else if (progress > 1) {
            progress = 1;
        }
        bossBar.setProgress((float) Math.max(0.0, bossBar.getProgress() - timeDecrease));
        bossBar.setTitle(bossBarText);
        if (remainingProtectionTime < 0) {
            bossBar.removePlayer(player);
            listener.remove(uuid);
            Bukkit.getScheduler().cancelTask(taskId);
            }
        return;
    }

    public void startProgressUpdater() {
        Player player = Bukkit.getPlayer(uuid);
        long remainingProtectionTime = scheduledRemovalTime;
        String bossBarText = PlaceholderAPI.setPlaceholders(player, getConfig().getString("bossbar_message","You are protected from PVP for %townywild_countdown%!"));
        bossBarText = bossBarText.replace("%townywild_countdown%", String.valueOf(remainingProtectionTime));
        bossBar.setTitle(bossBarText);
        if (remainingProtectionTime < 0) {
            listener.remove(uuid);
            return;
        }
        this.taskId = Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, 20L).getTaskId();
    }
}

