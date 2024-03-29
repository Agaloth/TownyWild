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
    private final double totalSeconds;
    private final long futureTime;
    private final Player player;

    // Creates a bossbar called timeLeftBar and gets the messages, colors and style from the config file
    BossBar timeLeftBar = Bukkit.createBossBar(ChatColor.BLUE + "You are protected for %townywild_countdown%",
            BarColor.BLUE,
            BarStyle.SOLID);

    public UpdateBossBarProgress(Player player, long remainingTime) {
        this.player = player;
        // This is a formula used to create a cooldown by adding futureTime to the protectionExpirationTime hashmap on the run method, check TownyWildPlaceholderExpansion to see how it is used.
        this.futureTime = System.currentTimeMillis() + (Integer.parseInt(Objects.requireNonNull(getConfig().getString("protection_time_after_exiting_town_border")))) * 1000L;

        // Used in the formula on the run method, it gets the protection time from the config file and divides it by 10.
        this.totalSeconds = (Integer.parseInt(Objects.requireNonNull(getConfig().getString("protection_time_after_exiting_town_border")))) / 10D;

        // Adds the player's uuid and the timeLeftBar bossbar to the createBossBar hashmap
        createBossBar.put(player.getUniqueId(), timeLeftBar);
    }

    @Override
    public void run() {

        // Uses a formula doing 0.1 divided by the total seconds left.
        double timeDecrease = (double) 0.1 / totalSeconds;

        // Sets the bossbar progress to decrement each second with the formula.
        timeLeftBar.setProgress((float) Math.max(0.0, timeLeftBar.getProgress() - timeDecrease));

        // Adds the future time to the protectionExpirationTime hashmap which is the current time in milliseconds + the amount of protection time multiplied by 1000.
        protectionExpirationTime.put(player.getUniqueId(), futureTime);

        // Translates the %townywild_countdown% placeholder and gets the text, color and style from config files
        String bossBarText = PlaceholderAPI.setPlaceholders(player, getConfig().getString("bossbar_message", "You are protected for %townywild_countdown%!"));
        String bossbarColor = getConfig().getString("bossbar_color");
        String bossbarStyle = getConfig().getString("bossbar_style");

        // Adds color support to the bossbar text
        bossBarText = ChatColor.translateAlternateColorCodes('&', bossBarText);

        // Sets the title to bossBarText with the translated %townywild_countdown% placeholder
        timeLeftBar.setTitle(bossBarText);
        timeLeftBar.setColor(BarColor.valueOf(bossbarColor));
        timeLeftBar.setStyle(BarStyle.valueOf(bossbarStyle));

        // Adds the player's uuid and the timeLeftBar bossbar to the createBossBar hashmap
        createBossBar.put(player.getUniqueId(), timeLeftBar);

        // If the progress hits 0, the task will be cancelled.
        if (((float) Math.max(0.0, timeLeftBar.getProgress() - timeDecrease)) > 0) return;

        timeLeftBar.setProgress(0);
        cancel();
        // Removes the future time from the protectionExpirationTime hashmap to run the task again until the bossbar ends.
        protectionExpirationTime.remove(player.getUniqueId());
    }
}
