package com.agaloth.townywild.listeners;

import com.agaloth.townywild.TownyWild;
import static com.agaloth.townywild.TownyWild.plugin;

import com.agaloth.townywild.tasks.UpdateBossBarProgress;

import static com.agaloth.townywild.hooks.TownyWildPlaceholderExpansion.protectionExpirationTime;
import static com.agaloth.townywild.settings.Settings.getConfig;
import static com.agaloth.townywild.tasks.UpdateBossBarProgress.*;
import static com.agaloth.townywild.tasks.UpdateBossBarProgress.uuid;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.event.TownClaimEvent;
import com.palmergames.bukkit.towny.event.town.TownUnclaimEvent;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.event.player.PlayerEntersIntoTownBorderEvent;
import com.palmergames.bukkit.towny.event.player.PlayerExitsFromTownBorderEvent;
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
    public void PlayerDamagePlayer(TownyPlayerDamagePlayerEvent event) {

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
    public void ExitTownBorder(PlayerExitsFromTownBorderEvent event) {

        // Gets the remaining time from the config file.
        int remainingTime = (Integer.parseInt(Objects.requireNonNull(getConfig().getString("protection_time_after_exiting_town_border"))));

        // If the HashSet for toggledProtection contains the UUID of the player then don't do anything.
        if (toggledProtection.contains(event.getPlayer().getUniqueId())) {

            } else {

                // Add player to the hashmap storing expiration times.
                protectionExpirationTime.put(event.getPlayer().getUniqueId(), (long) remainingTime * 1000L + System.currentTimeMillis());

                // Runs a Bukkit scheduler to remove the player from the cancelProtectionTask hashmap.
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> removePlayerIfExpired(event.getPlayer()), remainingTime * 20L);
                cancelProtectionTask.put(event.getPlayer().getUniqueId(), task);

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
    public void UnclaimTownEvent(TownUnclaimEvent event) {

        // Gets the remaining time from the config file.
        int remainingTime = (Integer.parseInt(Objects.requireNonNull(getConfig().getString("protection_time_after_exiting_town_border"))));

        // For each player that are online.
        for (Player player : Bukkit.getOnlinePlayers()) {

            // If the HashSet for toggledProtection contains the UUID of the player then don't do anything.
            if (toggledProtection.contains(player.getUniqueId())) {
                return;

            } else {

                // If the player's location is equal to the unclaimed world coordinates.
                if (WorldCoord.parseCoord(player.getLocation()).equals(event.getWorldCoord())) {

                    // Add player to the hashmap storing expiration times.
                    protectionExpirationTime.put(player.getUniqueId(), (long) remainingTime * 1000L + System.currentTimeMillis());

                    // Run a Bukkit scheduler to remove the player from the cancelProtectionTask hashmap.
                    BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> removePlayerIfExpired(player), remainingTime * 20L);
                    cancelProtectionTask.put(player.getUniqueId(), task);

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
    public void TownSpawn(PlayerTeleportEvent event) {

        // If the HashSet for toggledProtection contains the UUID of the player then don't do anything.
        if (toggledProtection.contains(event.getPlayer().getUniqueId())) {

        } else {

            // Gets the Bukkit task and cancels the protection task.
            if (cancelProtectionTask.containsKey(event.getPlayer().getUniqueId())) {
                cancelProtectionTask.get(event.getPlayer().getUniqueId()).cancel();
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

                // Removes a player's UUID from the protectionExpirationTime list when entering a town.
                protectionExpirationTime.remove(event.getPlayer().getUniqueId());
            }
        }
    }

    @EventHandler
    public void TownClaim(TownClaimEvent event) {

        // If the HashSet for toggledProtection contains the UUID of the player then don't do anything.
        if (toggledProtection.contains(event.getResident().getUUID())) {

        } else {

            // Gets the Bukkit task and cancels the protection task.
            if (cancelProtectionTask.containsKey(event.getResident().getUUID())) {
                cancelProtectionTask.get(event.getResident().getUUID()).cancel();
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

                // Removes a player's UUID from the protectionExpirationTime list when entering a town.
                protectionExpirationTime.remove(event.getResident().getUUID());
            }
        }
    }

    @EventHandler
    public void BlacklistedWorlds(PlayerChangedWorldEvent event) {

        // Gets the player.
        Player player = event.getPlayer();

        // If the HashSet for toggledProtection contains the UUID of the player then don't do anything.
        if (toggledProtection.contains(event.getPlayer().getUniqueId())) {

        } else {

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

                    // Removes a player's UUID from the protectionExpirationTime list when entering a town.
                    protectionExpirationTime.remove(player.getUniqueId());
                }
            }
        }
    }

    @EventHandler
    public void EnterTownBorder(PlayerEntersIntoTownBorderEvent event) {

        // If the HashSet for toggledProtection contains the UUID of the player then don't do anything.
        if (toggledProtection.contains(event.getPlayer().getUniqueId())) {

        } else {

            // Gets the Bukkit task and cancels the protection task.
            if (cancelProtectionTask.containsKey(event.getPlayer().getUniqueId())) {
                cancelProtectionTask.get(event.getPlayer().getUniqueId()).cancel();
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

                // Removes a player's UUID from the protectionExpirationTime list when entering a town.
                protectionExpirationTime.remove(event.getPlayer().getUniqueId());
            }
        }
    }

    public void removePlayerIfExpired(Player player) {

        // If the HashSet for toggledProtection contains the UUID of the player then don't do anything.
        if (toggledProtection.contains(player.getUniqueId())) {

        } else {
            // Removes a player from the protectionExpirationTime hashmap.
            protectionExpirationTime.remove(player.getUniqueId());

            // Removes the bossbar when the countdown hits 0 seconds.
            createBossBar.get(player.getUniqueId());

            // Removes the bossbar if a player enters a town.
            BossBar timeLeftBar = createBossBar.remove(player.getUniqueId());
            timeLeftBar.removePlayer(Objects.requireNonNull(player.getPlayer()));

            // Sends a message to the protected player telling them that their protection has ended.
            TownyMessaging.sendMessage(player, Translatable.of("plugin_prefix").append(Translatable.of("player_protection_ended")));
        }
    }
}
