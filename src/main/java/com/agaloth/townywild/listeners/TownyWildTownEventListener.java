package com.agaloth.townywild.listeners;

import com.agaloth.townywild.TownyWild;
import com.agaloth.townywild.tasks.RemoveProtectedPlayerTask;
import com.agaloth.townywild.utils.Messaging;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.event.player.PlayerEntersIntoTownBorderEvent;
import com.palmergames.bukkit.towny.event.player.PlayerExitsFromTownBorderEvent;
import com.palmergames.bukkit.towny.object.Translatable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static com.agaloth.townywild.TownyWild.getPlugin;
import static com.agaloth.townywild.TownyWild.plugin;
import static com.agaloth.townywild.settings.Settings.getConfig;

public class TownyWildTownEventListener implements Listener {
    public static Set<UUID> protectedPlayers = new HashSet<>();
    public static Map<UUID, Integer> protectionTimeLeft = new HashMap<>();

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
        Bukkit.getScheduler().runTaskTimer(plugin, new RemoveProtectedPlayerTask(uuid), 0, 20);
        System.out.println(protectionTimeLeft + " seconds left of protection");
        System.out.println("protectedPlayers after adding player: " + protectedPlayers);
    }

    @EventHandler
    public void EnterTownBorder(PlayerEntersIntoTownBorderEvent event) {
        // Gets a player's UUID
        UUID uuid = event.getPlayer().getUniqueId();
        System.out.println("Player just entered the town border");

        // Removes a player's UUID from the protectedPlayers list when entering a town
        protectedPlayers.remove(event.getPlayer().getUniqueId());
        System.out.println("protectedPlayers after removing player: " + protectedPlayers);
    }
}
