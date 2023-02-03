package com.agaloth.townywild.listeners;

import com.agaloth.townywild.TownyWild;
import static com.agaloth.townywild.TownyWild.plugin;

import com.agaloth.townywild.hooks.TownyWildPlaceholderExpansion;
import com.agaloth.townywild.tasks.RemoveProtectedPlayerTask;
import com.agaloth.townywild.utils.Messaging;
import static com.agaloth.townywild.settings.Settings.getConfig;

import com.google.common.cache.AbstractCache;
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

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Objects;

public class TownyWildTownEventListener implements Listener {
    public static Set<UUID> protectedPlayers = new HashSet<>();
    private final Map<UUID, BukkitTask> removeProtectedPlayerTask = new HashMap<>();

    public TownyWildTownEventListener(TownyWild instance) {

    }

    @EventHandler
    public void PlayerDamagePlayer(TownyPlayerDamagePlayerEvent event) {
        // Gets the player being attacked.
        Player victim = event.getVictimPlayer();

        // Gets the attacking player.
        Player attacker = event.getAttackingPlayer();

        // If the protectedPlayers list contains the victim's UUID, it will cancel damages and send a message to the attacker.
        if (protectedPlayers.contains(victim.getUniqueId())) {
            event.setCancelled(true);
            Messaging.sendMsg(attacker, Translatable.of("attacking_player_message"));
            return;
        }
        // If the protectedPlayers list contains the attacker's UUID, it will cancel damages and send a message to the player being attacked.
        if (protectedPlayers.contains(attacker.getUniqueId())) {
            event.setCancelled(true);
            Messaging.sendMsg(attacker, Translatable.of("victim_player_message"));
        }
    }

    @EventHandler
    public void ExitTownBorder(PlayerExitsFromTownBorderEvent event) {
        // Gets a player's UUID.
        UUID uuid = event.getPlayer().getUniqueId();
        System.out.println("Player just exited the town border");

        // Adds a player's UUID to the protectedPlayers list when exiting a town.
        protectedPlayers.add(event.getPlayer().getUniqueId());

        // Gets the remaining time from the config file
        int remainingTime = (Integer.parseInt(Objects.requireNonNull(getConfig().getString("protection_time_after_exiting_town_border"))));

        // Runs a Bukkit scheduler to remove the player from the protectedPlayers hashmap
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, new RemoveProtectedPlayerTask(uuid), remainingTime*20L);

        // Adds the Bukkit task to a hashmap to cancel it when a player enters a town.
        removeProtectedPlayerTask.put(uuid, task);

        System.out.println("protectedPlayers after adding player: " + protectedPlayers);
    }

    @EventHandler
    public void EnterTownBorder(PlayerEntersIntoTownBorderEvent event) {
        // Gets a player's UUID
        UUID uuid = event.getPlayer().getUniqueId();
        System.out.println("Player just entered the town border");

        // Gets the Bukkit task and cancels it.
        if (removeProtectedPlayerTask.containsKey(uuid)) {
            removeProtectedPlayerTask.get(uuid).cancel();
        }

        // Removes a player's UUID from the protectedPlayers list when entering a town
        protectedPlayers.remove(event.getPlayer().getUniqueId());
        System.out.println("protectedPlayers after removing player: " + protectedPlayers);
    }
}
