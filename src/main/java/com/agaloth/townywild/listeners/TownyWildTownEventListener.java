package com.agaloth.townywild.listeners;

import com.agaloth.townywild.TownyWild;
import static com.agaloth.townywild.TownyWild.plugin;

import com.agaloth.townywild.utils.Messaging;

import static com.agaloth.townywild.hooks.TownyWildPlaceholderExpansion.protectionExpirationTime;
import static com.agaloth.townywild.settings.Settings.getConfig;

import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.event.player.PlayerEntersIntoTownBorderEvent;
import com.palmergames.bukkit.towny.event.player.PlayerExitsFromTownBorderEvent;
import com.palmergames.bukkit.towny.object.Translatable;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
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
    public Map<Player, BukkitTask> cancelProtectionTask = new HashMap<>();
    public Map<Player, BukkitTask> cancelBossbarTask = new HashMap<>();
    public Map<UUID, BossBar> bossBar = new HashMap<>();
    private UUID uuid;

    public TownyWildTownEventListener(TownyWild instance) {

    }

    @EventHandler
    public void PlayerDamagePlayer(TownyPlayerDamagePlayerEvent event) {
        // Gets the player being attacked.
        Player victim = event.getVictimPlayer();

        // Gets the attacking player.
        Player attacker = event.getAttackingPlayer();

        // If the protectedPlayers list contains the victim's UUID, it will cancel damages and send a message to the attacker.
        if (protectionExpirationTime.containsKey(victim)) {
            event.setCancelled(true);
            Messaging.sendMsg(attacker, Translatable.of("attacking_player_message"));
            return;
        }
        // If the protectedPlayers list contains the attacker's UUID, it will cancel damages and send a message to the player being attacked.
        if (protectionExpirationTime.containsKey(attacker)) {
            event.setCancelled(true);
            Messaging.sendMsg(attacker, Translatable.of("victim_player_message"));
        }
    }

    @EventHandler
    public void ExitTownBorder(PlayerExitsFromTownBorderEvent event) {
        System.out.println("Player just exited the town border");

        // Gets the remaining time from the config file
        int remainingTime = (Integer.parseInt(Objects.requireNonNull(getConfig().getString("protection_time_after_exiting_town_border"))));
        
        // Gets the player's uuid
        Player player = Bukkit.getPlayer(uuid);

        // Add player to the hashmap storing expiration times.
        protectionExpirationTime.put(event.getPlayer(), (long) remainingTime*1000L + System.currentTimeMillis());

        // Runs a Bukkit scheduler to remove the player from the cancelProtectionTask hashmap
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> removePlayerIfExpired(event.getPlayer()), remainingTime*20L);
        cancelProtectionTask.put(event.getPlayer(), task);

        // Runs a Bukkit scheduler to remove the player from the bossBarTask hashmap
        BukkitTask bossBarTask = Bukkit.getScheduler().runTaskLater(plugin, () -> removePlayerBarIfExpired(event.getPlayer()), remainingTime*20L);
        cancelBossbarTask.put(event.getPlayer(), bossBarTask);
        
        // Creates a bossbar called timeLeftBar and gets the messages, colors and style from the config file
        BossBar timeLeftBar = Bukkit.createBossBar((getConfig().getString("bossbar_message")),
                BarColor.valueOf(Objects.requireNonNull(getConfig().getString("bossbar_color"))),
                BarStyle.valueOf(Objects.requireNonNull(getConfig().getString("bossbar_style")).toUpperCase()));
        
        // Translates the %townywild_countdown% placeholder in the bossbar_message config line
        String bossBarText = PlaceholderAPI.setPlaceholders(player, getConfig().getString("bossbar_message","You are protected from PVP for %townywild_countdown%!"));
        
        // Adds color support to the bossbar text
        bossBarText = ChatColor.translateAlternateColorCodes('&', bossBarText);
        
        // Shows the bossbar to the player
        timeLeftBar.addPlayer(event.getPlayer());
        
        // Sets the title to bossBarText with the translated %townywild_countdown% placeholder
        timeLeftBar.setTitle(bossBarText);
    }
    public void removePlayerIfExpired(Player player)
    {
        // Removes a player from the protectedPlayers hashmap.
        protectionExpirationTime.remove(player);

        // Sends a message to the protected player telling them that their protection has ended.
        Messaging.sendMsg(player, Translatable.of("player_protection_ended"));
    }
    public void removePlayerBarIfExpired(Player player)
    {
        // Removes a player from the bossBar hashmap.
        bossBar.remove(uuid);
    }

    @EventHandler
    public void EnterTownBorder(PlayerEntersIntoTownBorderEvent event) {
        // Gets a player's UUID
        UUID uuid = event.getPlayer().getUniqueId();
        System.out.println("Player just entered the town border");

        // Gets the Bukkit task and cancels the protection task.
        if (cancelProtectionTask.containsKey(event.getPlayer())) {
            cancelProtectionTask.get(event.getPlayer()).cancel();
        }
        
        // Gets the Bukkit task and cancels the bossbar task.
        if (cancelBossbarTask.containsKey(event.getPlayer())) {
            cancelBossbarTask.get(event.getPlayer()).cancel();
        }

        // Removes a player's UUID from the protectedPlayers list when entering a town
        protectionExpirationTime.remove(event.getPlayer());
        System.out.println("protectedPlayers after removing player: " + protectionExpirationTime);
    }
}
