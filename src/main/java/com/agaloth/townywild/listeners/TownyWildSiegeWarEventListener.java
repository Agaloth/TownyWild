package com.agaloth.townywild.listeners;

import com.agaloth.townywild.TownyWild;
import com.agaloth.townywild.tasks.UpdateBossBarProgress;
import com.gmail.goosius.siegewar.SiegeController;
import com.gmail.goosius.siegewar.events.SiegeWarStartEvent;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

import static com.agaloth.townywild.TownyWild.plugin;
import static com.agaloth.townywild.TownyWild.siegeWarPresent;
import static com.agaloth.townywild.hooks.TownyWildPlaceholderExpansion.protectionExpirationTime;
import static com.agaloth.townywild.listeners.TownyWildTownEventListener.*;
import static com.agaloth.townywild.settings.ConfigNodes.*;
import static com.agaloth.townywild.settings.Settings.getBoolean;
import static com.agaloth.townywild.settings.Settings.getConfig;
import static com.agaloth.townywild.tasks.UpdateBossBarProgress.createBossBar;

public class TownyWildSiegeWarEventListener implements Listener {

    public TownyWildSiegeWarEventListener(TownyWild instance) {
    }

    @EventHandler
    public void siegeProtection(SiegeWarStartEvent event) {

        // Gets the remaining time from the config file.
        int remainingTime = (Integer.parseInt(Objects.requireNonNull(getConfig().getString("protection_time_after_exiting_town_border"))));

        // For loop to get each residents of the town that is being attacked.
        for (Resident resident : event.getTargetTown().getResidents()) {

            // If the HashSet for toggledProtection contains the UUID of the residents then don't do anything.
            if (toggledProtection.contains(resident.getUUID())) {

            } else {

                // If the Siege War plugin is present on the server and the defender's town has an ongoing siege, run the code below.
                if (siegeWarPresent() && SiegeController.hasSiege(event.getTargetTown())) {

                    // If the siege_war_start_protection config line is set to false, don't do anything.
                    if (!getBoolean(SIEGE_WAR_START_PROTECTION)) {
                        return;
                    }

                    // If the protection_after_exiting_town_border config line is set to false, don't do anything.
                    if (!getBoolean(PROTECTION_AFTER_EXITING_TOWN_BORDER)) {
                        return;
                    }

                    // Add residents of the defenders town to the hashmap storing expiration times.
                    protectionExpirationTime.put(resident.getUUID(), (long) remainingTime * 1000L + System.currentTimeMillis());

                    // Runs a Bukkit scheduler to remove the residents of the defenders town from the cancelProtectionTask hashmap.
                    BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> removePlayerIfExpired(Objects.requireNonNull(resident.getPlayer())), remainingTime * 20L);
                    cancelProtectionTask.put(resident.getUUID(), task);


                    // If the bossbar_enabled config line is set to false, don't show the bossbar to the player
                    if (!getBoolean(BOSSBAR_ENABLED)) {
                        return;
                    }

                    // Runs a Bukkit scheduler to update the bossbar progress and adds the residents of the defenders town to the runningBossBars hashmap to remove it when entering a town.
                    BukkitTask updateProgress = new UpdateBossBarProgress(Objects.requireNonNull(resident.getPlayer()), remainingTime).runTaskTimer(plugin, 0, 20);
                    runningBossBars.put(resident.getUUID(), updateProgress);

                    // Shows the bossbar to the residents of the defenders town.
                    BossBar timeLeftBar = createBossBar.get(resident.getUUID());

                    // Adds the residents of the defenders town to the timeLeftBar bossbar.
                    timeLeftBar.addPlayer(resident.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void removeAttackerProtection(SiegeWarStartEvent event) {

        // For loop to get each residents of the town that is attacking.
        for (Resident attacker : event.getTownOfSiegeStarter().getResidents()) {

            // If the HashSet for toggledProtection contains the UUID of the attacker then don't do anything.
            if (toggledProtection.contains(attacker.getUUID())) {


            } else {

                // If the Siege War plugin is present on the server and the town that the player is entering has an ongoing siege, don't run any of the code.
                if (siegeWarPresent() && SiegeController.hasSiege(event.getTownOfSiegeStarter())) {
                    return;
                }

                // If the protection_after_exiting_town_border config line is set to false, don't do anything.
                if (!getBoolean(PROTECTION_AFTER_EXITING_TOWN_BORDER)) {
                    return;
                }

                // Gets the Bukkit task and cancels the protection task.
                if (cancelProtectionTask.containsKey(attacker.getUUID())) {
                    cancelProtectionTask.get(attacker.getUUID()).cancel();
                }

                // Removes a attacker's UUID from the protectionExpirationTime list when entering a town.
                protectionExpirationTime.remove(attacker.getUUID());

                // If the bossbar_enabled config line is set to false, don't show the bossbar to the player
                if (!getBoolean(BOSSBAR_ENABLED)) {
                    return;
                }

                // Gets the Bukkit task and cancels the bossbar progress task and sets the progress to 1 to prevent the progress bar from starting at random numbers.
                if (runningBossBars.containsKey(attacker.getUUID())) {
                    runningBossBars.get(attacker.getUUID()).cancel();
                    runningBossBars.remove(attacker.getUUID());

                    // Removes the bossbar if an attacker starts a siege war.
                    BossBar timeLeftBar = createBossBar.remove(attacker.getUUID());

                    // If the timeLeftBar expired don't do anything but if it's still running then remove the timeLeftBar bossbar when starting a siege war.
                    if (timeLeftBar == null) {
                        return;
                    } else {
                        timeLeftBar.removePlayer(attacker.getPlayer());
                    }
                }
            }
        }
    }
}
