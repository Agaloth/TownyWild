package com.agaloth.townywild.tasks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.UUID;

import static com.agaloth.townywild.settings.Settings.getConfig;

public class UpdateBossBarProgress extends BukkitRunnable {
    private final double totalSeconds;

    public static UUID uuid;

    // Gets the player's uuid
    Player player = Bukkit.getPlayer(uuid);

    // Creates a bossbar called timeLeftBar and gets the messages, colors and style from the config file
    public static BossBar timeLeftBar = Bukkit.createBossBar(
            ChatColor.YELLOW + "You are protected for %townywild_countdown",
            BarColor.YELLOW,
            BarStyle.SOLID);


    public UpdateBossBarProgress(Player player) {
        // Used in the formula below, it gets the protection time from the config file and divides it by 10.
        this.totalSeconds = (Integer.parseInt(Objects.requireNonNull(getConfig().getString("protection_time_after_exiting_town_border")))) / 10D;
    }

    @Override
    public void run() {
        // Uses a formula doing 0.1 divided by the total seconds left.
        double timeDecrease = (double) 0.1 / totalSeconds;

           // Sets the bossbar progress to decrement each second with the formula.
           timeLeftBar.setProgress((float) Math.max(0.0, timeLeftBar.getProgress() - timeDecrease));

           // Translates the %townywild_countdown% placeholder and gets the text, color and style from config files
           String bossBarText = PlaceholderAPI.setPlaceholders(player, getConfig().getString("bossbar_message","You are protected for %townywild_countdown%!"));
           String bossbarColor = getConfig().getString("bossbar_color");
           String bossbarStyle = getConfig().getString("bossbar_style");

           // Adds color support to the bossbar text
           bossBarText = ChatColor.translateAlternateColorCodes('&', bossBarText);

           // Sets the title to bossBarText with the translated %townywild_countdown% placeholder
           timeLeftBar.setTitle(bossBarText);
           timeLeftBar.setColor(BarColor.valueOf(bossbarColor));
           timeLeftBar.setStyle(BarStyle.valueOf(bossbarStyle));

            // If the progress hits 0, the task will be cancelled.
            if (((float) Math.max(0.0, timeLeftBar.getProgress() - timeDecrease)) == 0) {
                timeLeftBar.setProgress(0);
            }
        }
    }
