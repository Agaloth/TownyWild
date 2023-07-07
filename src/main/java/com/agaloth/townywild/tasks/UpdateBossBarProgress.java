package com.agaloth.townywild.tasks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.agaloth.townywild.hooks.TownyWildPlaceholderExpansion.protectionExpirationTime;
import static com.agaloth.townywild.settings.Settings.getConfig;

public class UpdateBossBarProgress extends BukkitRunnable implements Listener {
    public static Map<UUID, BossBar> createBossBar = new HashMap<>();
    public static Map<UUID, UpdateBossBarProgress> updateBossBar = new HashMap<>();
    private final double totalSeconds;
    private final long futureTime;
    private final UUID uuid;

    public UpdateBossBarProgress(UUID uuid, long remainingTime) {
        this.uuid = uuid;
        this.futureTime = System.currentTimeMillis() + (Integer.parseInt(Objects.requireNonNull(getConfig().getString("protection_time_after_exiting_town_border")))) * 1000L;
        this.totalSeconds = (Integer.parseInt(Objects.requireNonNull(getConfig().getString("protection_time_after_exiting_town_border")))) / 10D;
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        String bossbarColor = getConfig().getString("bossbar_color");
        String bossbarStyle = getConfig().getString("bossbar_style");
        BossBar timeLeftBar = Bukkit.createBossBar("You are protected for %townywild_countdown%", BarColor.valueOf(bossbarColor), BarStyle.valueOf(bossbarStyle));
        if (updateBossBar.containsKey(player.getUniqueId())) {
            updateBossBar.remove(player.getUniqueId()).cancel();
        }
        updateBossBar.put(player.getUniqueId(), this);
        createBossBar.put(player.getUniqueId(), timeLeftBar);
    }

    @Override
    public void run() {
        Player player = Bukkit.getPlayer(uuid);
        BossBar timeLeftBar = createBossBar.get(uuid);
        if (player == null || timeLeftBar == null) {
            cancel(); // Cancel the task if the player or boss bar is not available
            updateBossBar.remove(uuid);
            return;
        }
        // Uses a formula doing 0.1 divided by the total seconds left.
        double timeDecrease = 0.1 / totalSeconds;

        // Sets the bossbar progress to decrement each second with the formula.
        timeLeftBar.setProgress((float) Math.max(0.0, timeLeftBar.getProgress() - timeDecrease));

        System.out.println("UUID: " + uuid);
        // Adds the future time to the protectionExpirationTime hashmap which is the current time in milliseconds + the amount of protection time multiplied by 1000.
        protectionExpirationTime.put(uuid, futureTime);

        // Translates the %townywild_countdown% placeholder and gets the text, color and style from config files
        String bossBarText = PlaceholderAPI.setPlaceholders(player, getConfig().getString("bossbar_message", "You are protected for %townywild_countdown%!"));
        // Adds color support to the bossbar text
        bossBarText = ChatColor.translateAlternateColorCodes('&', bossBarText);
        // Sets the title to bossBarText with the translated %townywild_countdown% placeholder
        timeLeftBar.setTitle(bossBarText);
        // If the progress hits 0, the task will be cancelled.
        if (((float) Math.max(0.0, timeLeftBar.getProgress() - timeDecrease)) == 0) {
            timeLeftBar.setProgress(0);
            cancel();
        }
        System.out.println("Removing: " + uuid);
        // Removes the future time from the protectionExpirationTime hashmap to run the task again until the bossbar ends.
        protectionExpirationTime.remove(uuid);

        // Adds the player's uuid and the timeLeftBar bossbar to the createBossBar hashmap
        createBossBar.put(uuid, timeLeftBar);
    }
}