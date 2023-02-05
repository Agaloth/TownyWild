package com.agaloth.townywild.listeners;

import com.agaloth.townywild.TownyWild;
import static com.agaloth.townywild.TownyWild.plugin;

import com.agaloth.townywild.tasks.UpdateBossBarProgress;

import static com.agaloth.townywild.hooks.TownyWildPlaceholderExpansion.protectionExpirationTime;
import static com.agaloth.townywild.settings.Settings.getConfig;
import static com.agaloth.townywild.tasks.UpdateBossBarProgress.*;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.event.player.PlayerEntersIntoTownBorderEvent;
import com.palmergames.bukkit.towny.event.player.PlayerExitsFromTownBorderEvent;
import com.palmergames.bukkit.towny.object.Translatable;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Objects;

public class TownyWildTownEventListener implements Listener {
    public Map<UUID, BukkitTask> cancelProtectionTask = new HashMap<>();
    public Map<UUID, BukkitTask> runningBossBars = new HashMap<>();

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

        // Add player to the hashmap storing expiration times.
        protectionExpirationTime.put(event.getPlayer().getUniqueId(), (long) remainingTime*1000L + System.currentTimeMillis());

        // Runs a Bukkit scheduler to remove the player from the cancelProtectionTask hashmap.
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> removePlayerIfExpired(event.getPlayer()), remainingTime*20L);
        cancelProtectionTask.put(event.getPlayer().getUniqueId(), task);

        // Runs a Bukkit scheduler to update the bossbar progress and adds the player to the runningBossBars hashmap to remove it when entering a town.
        BukkitTask updateProgress = new UpdateBossBarProgress(event.getPlayer(), remainingTime).runTaskTimer(plugin, 0, 20);
        runningBossBars.put(event.getPlayer().getUniqueId(), updateProgress);

        // Shows the bossbar to the player.
        BossBar timeLeftBar = createBossBar.get(event.getPlayer().getUniqueId());

        // Adds the player to the timeLeftBar bossbar.
        timeLeftBar.addPlayer(event.getPlayer());
    }

    @EventHandler
    public void EnterTownBorder(PlayerEntersIntoTownBorderEvent event) {
        // Gets a player's UUID
        UUID uuid = event.getPlayer().getUniqueId();

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
    public void removePlayerIfExpired(Player player) {
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
