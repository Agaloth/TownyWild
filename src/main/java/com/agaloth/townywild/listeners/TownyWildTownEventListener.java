package com.agaloth.townywild.listeners;

import com.agaloth.townywild.TownyWild;
import static com.agaloth.townywild.TownyWild.plugin;

import com.agaloth.townywild.tasks.UpdateBossBarProgress;

import static com.agaloth.townywild.TownyWild.siegeWarPresent;
import static com.agaloth.townywild.hooks.TownyWildPlaceholderExpansion.protectionExpirationTime;
import static com.agaloth.townywild.settings.ConfigNodes.*;
import static com.agaloth.townywild.settings.Settings.getBoolean;
import static com.agaloth.townywild.settings.Settings.getConfig;
import static com.agaloth.townywild.tasks.UpdateBossBarProgress.*;

import com.gmail.goosius.siegewar.SiegeController;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.event.TownClaimEvent;
import com.palmergames.bukkit.towny.event.town.TownUnclaimEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.event.player.PlayerEntersIntoTownBorderEvent;
import com.palmergames.bukkit.towny.event.player.PlayerExitsFromTownBorderEvent;
import com.gmail.goosius.siegewar.events.SiegeWarStartEvent;
import com.palmergames.bukkit.towny.object.Translatable;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Objects;

public class TownyWildTownEventListener implements Listener {
    public static Map<UUID, BukkitTask> cancelProtectionTask = new HashMap<>();
    public static Map<UUID, BukkitTask> runningBossBars = new HashMap<>();
    public static HashSet<UUID> toggledProtection = new HashSet<>();

    public TownyWildTownEventListener(TownyWild instance) {
    }

    @EventHandler
    public void playerDamagePlayer(TownyPlayerDamagePlayerEvent event) {

        // Gets the player being attacked.
        Player victim = event.getVictimPlayer();

        // Gets the attacking player.
        Player attacker = event.getAttackingPlayer();

        // If the protectionExpirationTime list contains the victim's UUID, it will cancel damages and send a message to the attacker.
        if (protectionExpirationTime.containsKey(victim.getUniqueId())) {
            event.setCancelled(true);
            TownyMessaging.sendMessage(attacker, Translatable.of("plugin_prefix").append(Translatable.of("victim_player_message")));
            return;
        }

        // If the protectionExpirationTime list contains the attacker's UUID, it will cancel damages and send a message to the player being attacked.
        if (protectionExpirationTime.containsKey(attacker.getUniqueId())) {
            event.setCancelled(true);
            TownyMessaging.sendMessage(attacker, Translatable.of("plugin_prefix").append(Translatable.of("victim_player_message")));
        }
    }

    @EventHandler
    public void exitTownBorder(PlayerExitsFromTownBorderEvent event) {

        // Gets the remaining time from the config file.
        int remainingTime = (Integer.parseInt(Objects.requireNonNull(getConfig().getString("protection_time_after_exiting_town_border"))));

        // If the HashSet for toggledProtection contains the UUID of the player then don't do anything.
        if (toggledProtection.contains(event.getPlayer().getUniqueId())) {

        } else {

            // If the Siege War plugin is present on the server and the town that the player is leaving has an ongoing siege, don't do anything.
            if (siegeWarPresent() && SiegeController.hasSiege(event.getLeftTown())) {
                return;
            }

            // If the protection_after_exiting_town_border config is set to false, don't do anything.
            if (!getBoolean(PROTECTION_AFTER_EXITING_TOWN_BORDER)) {
                return;
            }

                // Add player to the hashmap storing expiration times.
                protectionExpirationTime.put(event.getPlayer().getUniqueId(), (long) remainingTime * 1000L + System.currentTimeMillis());

                // Runs a Bukkit scheduler to remove the player from the cancelProtectionTask hashmap.
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> removePlayerIfExpired(event.getPlayer()), remainingTime * 20L);
                cancelProtectionTask.put(event.getPlayer().getUniqueId(), task);

                // If the bossbar_enabled config line is set to false, don't show the bossbar to the player
                if (!getBoolean(BOSSBAR_ENABLED)) {
                    return;
                }

                // Runs a Bukkit scheduler to update the bossbar progress and adds the player to the runningBossBars hashmap to remove it when entering a town.
                BukkitTask updateProgress = new UpdateBossBarProgress(event.getPlayer(), remainingTime).runTaskTimer(plugin, 0, 20);
                runningBossBars.put(event.getPlayer().getUniqueId(), updateProgress);

                // Shows the bossbar to the player.
                BossBar timeLeftBar = createBossBar.get(event.getPlayer().getUniqueId());

                // Adds the player to the timeLeftBar bossbar.
                timeLeftBar.addPlayer(event.getPlayer());
            }
        }

    @EventHandler
    public void unclaimTownEvent(TownUnclaimEvent event) {

        // Gets the remaining time from the config file.
        int remainingTime = (Integer.parseInt(Objects.requireNonNull(getConfig().getString("protection_time_after_exiting_town_border"))));

        // For each player that are online.
        for (Player player : Bukkit.getOnlinePlayers()) {

            // If the HashSet for toggledProtection contains the UUID of the player then don't do anything.
            if (toggledProtection.contains(player.getUniqueId())) {
                return;

            } else {

                // If the protection_after_exiting_town_border config is set to false, don't do anything.
                if (!getBoolean(PROTECTION_AFTER_EXITING_TOWN_BORDER)) {
                    return;
                }

                // If the player's location is equal to the unclaimed world coordinates.
                if (WorldCoord.parseCoord(player.getLocation()).equals(event.getWorldCoord())) {

                    // Add player to the hashmap storing expiration times.
                    protectionExpirationTime.put(player.getUniqueId(), (long) remainingTime * 1000L + System.currentTimeMillis());

                    // Run a Bukkit scheduler to remove the player from the cancelProtectionTask hashmap.
                    BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> removePlayerIfExpired(player), remainingTime * 20L);
                    cancelProtectionTask.put(player.getUniqueId(), task);

                    // If the bossbar_enabled config line is set to false, don't show the bossbar to the player
                    if (!getBoolean(BOSSBAR_ENABLED)) {
                        return;
                    }

                    // Run a Bukkit scheduler to update the bossbar progress and add the player to the runningBossBars hashmap.
                    BukkitTask updateProgress = new UpdateBossBarProgress(player, remainingTime).runTaskTimer(plugin, 0, 20);
                    runningBossBars.put(player.getUniqueId(), updateProgress);

                    // Show the bossbar to the player.
                    BossBar timeLeftBar = createBossBar.get(player.getUniqueId());

                    // Add the player to the timeLeftBar bossbar.
                    timeLeftBar.addPlayer(player);
                }
            }
        }
    }

    @EventHandler
    public void townSpawn(PlayerTeleportEvent event) {

        // If the HashSet for toggledProtection contains the UUID of the player then don't do anything.
        if (toggledProtection.contains(event.getPlayer().getUniqueId())) {

        } else {

            // If the protection_after_exiting_town_border config is set to false, don't do anything.
            if (!getBoolean(PROTECTION_AFTER_EXITING_TOWN_BORDER)) {
                return;
            }

            // Gets the Bukkit task and cancels the protection task.
            if (cancelProtectionTask.containsKey(event.getPlayer().getUniqueId())) {
                cancelProtectionTask.get(event.getPlayer().getUniqueId()).cancel();
            }

            // Removes a player's UUID from the protectionExpirationTime list when entering a town.
            protectionExpirationTime.remove(event.getPlayer().getUniqueId());

            // If the bossbar_enabled config line is set to false, don't show the bossbar to the player
            if (!getBoolean(BOSSBAR_ENABLED)) {
                return;
            }

            // Gets the Bukkit task and cancels the bossbar progress task and sets the progress to 1 to prevent the progress bar from starting at random numbers.
            if (runningBossBars.containsKey(event.getPlayer().getUniqueId())) {
                runningBossBars.get(event.getPlayer().getUniqueId()).cancel();
                runningBossBars.remove(event.getPlayer().getUniqueId());

                // Removes the bossbar if a player enters a town.
                BossBar timeLeftBar = createBossBar.remove(event.getPlayer().getUniqueId());

                // If the timeLeftBar expired don't do anything but if it's still running then remove the timeLeftBar bossbar when entering a town.
                if (timeLeftBar == null) {
                    return;
                } else {
                    timeLeftBar.removePlayer(event.getPlayer());
                }

            }
        }
    }

    @EventHandler
    public void townClaim(TownClaimEvent event) {

        // If the HashSet for toggledProtection contains the UUID of the player then don't do anything.
        if (toggledProtection.contains(event.getResident().getUUID())) {

        } else {

            // If the protection_after_exiting_town_border config is set to false, don't do anything.
            if (!getBoolean(PROTECTION_AFTER_EXITING_TOWN_BORDER)) {
                return;
            }

            // Gets the Bukkit task and cancels the protection task.
            if (cancelProtectionTask.containsKey(event.getResident().getUUID())) {
                cancelProtectionTask.get(event.getResident().getUUID()).cancel();
            }

            // Removes a player's UUID from the protectionExpirationTime list when entering a town.
            protectionExpirationTime.remove(event.getResident().getUUID());

            // If the bossbar_enabled config line is set to false, don't show the bossbar to the player
            if (!getBoolean(BOSSBAR_ENABLED)) {
                return;
            }

            // Gets the Bukkit task and cancels the bossbar progress task and sets the progress to 1 to prevent the progress bar from starting at random numbers.
            if (runningBossBars.containsKey(event.getResident().getUUID())) {
                runningBossBars.get(event.getResident().getUUID()).cancel();
                runningBossBars.remove(event.getResident().getUUID());

                // Removes the bossbar if a player enters a town.
                BossBar timeLeftBar = createBossBar.remove(event.getResident().getUUID());

                // If the timeLeftBar expired don't do anything but if it's still running then remove the timeLeftBar bossbar when entering a town.
                if (timeLeftBar == null) {
                    return;
                } else {
                    timeLeftBar.removePlayer(Objects.requireNonNull(event.getResident().getPlayer()));
                }
            }
        }
    }

    @EventHandler
    public void blacklistedWorlds(PlayerChangedWorldEvent event) {

        // Gets the player.
        Player player = event.getPlayer();

        // If the HashSet for toggledProtection contains the UUID of the player then don't do anything.
        if (toggledProtection.contains(event.getPlayer().getUniqueId())) {

        } else {

            // If the protection_after_exiting_town_border config is set to false, don't do anything.
            if (!getBoolean(PROTECTION_AFTER_EXITING_TOWN_BORDER)) {
                return;
            }

            // Creates a List for blacklisted worlds and gets them from the config file.
            List<String> blacklistedWorlds = new java.util.ArrayList<>(Collections.singletonList(getConfig().getString("blacklisted_worlds")));

            // Adds the current world's name that the player is in to the blacklistedWorlds List.
            blacklistedWorlds.add(player.getWorld().getName());

            // If the blacklistedWorlds List contains the world name that the player is in then run everything below.
            if (blacklistedWorlds.contains(player.getWorld().getName())) {

                // Gets the Bukkit task and cancels the protection task.
                if (cancelProtectionTask.containsKey(player.getUniqueId())) {
                    cancelProtectionTask.get(player.getUniqueId()).cancel();
                }

                // Removes a player's UUID from the protectionExpirationTime list when entering a town.
                protectionExpirationTime.remove(player.getUniqueId());

                // If the bossbar_enabled config line is set to false, don't show the bossbar to the player
                if (!getBoolean(BOSSBAR_ENABLED)) {
                    return;
                }

                // Gets the Bukkit task and cancels the bossbar progress task and sets the progress to 1 to prevent the progress bar from starting at random numbers.
                if (runningBossBars.containsKey(player.getUniqueId())) {
                    runningBossBars.get(player.getUniqueId()).cancel();
                    runningBossBars.remove(player.getUniqueId());

                    // Removes the bossbar if a player enters a town.
                    BossBar timeLeftBar = createBossBar.remove(player.getUniqueId());

                    // If the timeLeftBar expired don't do anything but if it's still running then remove the timeLeftBar bossbar when entering a town.
                    if (timeLeftBar == null) {
                        return;
                    } else {
                        timeLeftBar.removePlayer(player);
                    }
                }
            }
        }
    }

    @EventHandler
    public void enterTownBorder(PlayerEntersIntoTownBorderEvent event) {

        // If the HashSet for toggledProtection contains the UUID of the player then don't do anything.
        if (toggledProtection.contains(event.getPlayer().getUniqueId())) {

        } else {

            // If the Siege War plugin is present on the server and the town that the player is entering has an ongoing siege, don't run any of the code.
            if (siegeWarPresent() && SiegeController.hasSiege(event.getEnteredTown())) {
                return;
            }

            // If the protection_after_exiting_town_border config line is set to false, don't do anything.
            if (!getBoolean(PROTECTION_AFTER_EXITING_TOWN_BORDER)) {
                return;
            }

            // Gets the Bukkit task and cancels the protection task.
            if (cancelProtectionTask.containsKey(event.getPlayer().getUniqueId())) {
                cancelProtectionTask.get(event.getPlayer().getUniqueId()).cancel();
            }

            // Removes a player's UUID from the protectionExpirationTime list when entering a town.
            protectionExpirationTime.remove(event.getPlayer().getUniqueId());

            // If the bossbar_enabled config line is set to false, don't show the bossbar to the player
            if (!getBoolean(BOSSBAR_ENABLED)) {
                return;
            }

            // Gets the Bukkit task and cancels the bossbar progress task and sets the progress to 1 to prevent the progress bar from starting at random numbers.
            if (runningBossBars.containsKey(event.getPlayer().getUniqueId())) {
                runningBossBars.get(event.getPlayer().getUniqueId()).cancel();
                runningBossBars.remove(event.getPlayer().getUniqueId());

                // Removes the bossbar if a player enters a town.
                BossBar timeLeftBar = createBossBar.remove(event.getPlayer().getUniqueId());

                // If the timeLeftBar expired don't do anything but if it's still running then remove the timeLeftBar bossbar when entering a town.
                if (timeLeftBar == null) {
                    return;
                } else {
                    timeLeftBar.removePlayer(event.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void createTown(NewTownEvent event) {

        // For loop to get each residents of the town that is being created.
        for (Resident resident : event.getTown().getResidents()) {

            // If the HashSet for toggledProtection contains the UUID of the player then don't do anything.
            if (toggledProtection.contains(resident.getUUID())) {


            } else {

                // If the Siege War plugin is present on the server and the town that the player is entering has an ongoing siege, don't run any of the code.
                if (siegeWarPresent() && SiegeController.hasSiege(event.getTown())) {
                    return;
                }

                // If the protection_after_exiting_town_border config line is set to false, don't do anything.
                if (!getBoolean(PROTECTION_AFTER_EXITING_TOWN_BORDER)) {
                    return;
                }

                // Gets the Bukkit task and cancels the protection task.
                if (cancelProtectionTask.containsKey(resident.getUUID())) {
                    cancelProtectionTask.get(resident.getUUID()).cancel();
                }

                // Removes a player's UUID from the protectionExpirationTime list when entering a town.
                protectionExpirationTime.remove(resident.getUUID());

                // If the bossbar_enabled config line is set to false, don't show the bossbar to the player
                if (!getBoolean(BOSSBAR_ENABLED)) {
                    return;
                }

                // Gets the Bukkit task and cancels the bossbar progress task and sets the progress to 1 to prevent the progress bar from starting at random numbers.
                if (runningBossBars.containsKey(resident.getUUID())) {
                    runningBossBars.get(resident.getUUID()).cancel();
                    runningBossBars.remove(resident.getUUID());

                    // Removes the bossbar if a player enters a town.
                    BossBar timeLeftBar = createBossBar.remove(resident.getUUID());

                    // If the timeLeftBar expired don't do anything but if it's still running then remove the timeLeftBar bossbar when entering a town.
                    if (timeLeftBar == null) {
                        return;
                    } else {
                        timeLeftBar.removePlayer(resident.getPlayer());
                    }
                }
            }
        }
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

    public void removePlayerIfExpired(Player player) {

        // If the HashSet for toggledProtection contains the UUID of the player then don't do anything.
        if (toggledProtection.contains(player.getUniqueId())) {

        } else {

            // If the protection_after_exiting_town_border config line is set to false, don't do anything.
            if (!getBoolean(PROTECTION_AFTER_EXITING_TOWN_BORDER)) {
                return;
            }

            // Removes a player from the protectionExpirationTime hashmap.
            protectionExpirationTime.remove(player.getUniqueId());

            // Sends a message to the protected player telling them that their protection has ended.
            TownyMessaging.sendMessage(player, Translatable.of("plugin_prefix").append(Translatable.of("player_protection_ended")));

            // If the bossbar_enabled config line is set to false, don't show the bossbar to the player
            if (!getBoolean(BOSSBAR_ENABLED)) {
                return;
            }

            // Removes the bossbar when the countdown hits 0 seconds.
            createBossBar.get(player.getUniqueId());

            // Removes the bossbar if a player enters a town.
            BossBar timeLeftBar = createBossBar.remove(player.getUniqueId());
            timeLeftBar.removePlayer(Objects.requireNonNull(player.getPlayer()));
        }
    }
}
