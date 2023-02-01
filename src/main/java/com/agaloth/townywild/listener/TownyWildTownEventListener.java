package com.agaloth.townywild.listeners;

import com.agaloth.townywild.tasks.RemoveProtectedPlayerTask;
import com.agaloth.townywild.tasks.UpdateBossBarPlaceholder;
import com.palmergames.bukkit.towny.event.player.PlayerEntersIntoTownBorderEvent;
import com.palmergames.bukkit.towny.event.player.PlayerExitsFromTownBorderEvent;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;

import com.agaloth.townywild.TownyWild;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

import static com.agaloth.townywild.settings.Settings.getConfig;

public class TownyWildTownEventListener implements Listener {
    @SuppressWarnings("unused")
    private final TownyWild plugin;
    private Set<UUID> protectedPlayers = new HashSet<>();
    public static Map<UUID, Long> scheduledRemovalTimes = new HashMap<>();
    public static Map<UUID, BossBar> bossBars = new HashMap<>();
    private Map<UUID, Integer> bossBarTasks = new HashMap<>();

    public TownyWildTownEventListener(TownyWild instance) {

        plugin = instance;
    }

    @EventHandler
    public void on(TownyPlayerDamagePlayerEvent event) {
        Player victim = event.getVictimPlayer();
        Player attacker = event.getAttackingPlayer();
        if (protectedPlayers.contains(victim.getUniqueId())) {
            event.setCancelled(true);
            attacker.sendMessage(ChatColor.YELLOW + "You cannot attack an innocent person while he's on cooldown!");
            return;
        }
        if (protectedPlayers.contains(attacker.getUniqueId())) {
            event.setCancelled(true);
            attacker.sendMessage(ChatColor.YELLOW + "You cannot attack an enemy while on cooldown!");
        }
    }

    @EventHandler
    public void on(PlayerExitsFromTownBorderEvent event) {
        System.out.println("Player just exited the town border");
        UUID uuid = event.getPlayer().getUniqueId();
        protectedPlayers.add(event.getPlayer().getUniqueId());
        System.out.println("protectedPlayers after adding player: " + protectedPlayers);
        System.out.println("Player is now protected");
        long scheduledRemovalTime = (Integer.parseInt(Objects.requireNonNull(getConfig().getString("protection_time_after_exiting_town_border"))));
        System.out.println(scheduledRemovalTime + " seconds left");
        scheduledRemovalTimes.put(uuid, scheduledRemovalTime);
        String bossBarText = PlaceholderAPI.setPlaceholders(event.getPlayer(), getConfig().getString("bossbar_message", "You are protected from PVP for %townywild_countdown%!"));
        bossBarText = PlaceholderAPI.setPlaceholders(event.getPlayer(), bossBarText);
        BarColor bossBarColor = BarColor.valueOf(getConfig().getString("bossbar_color", "YELLOW"));
        BarStyle bossBarStyle = BarStyle.valueOf(getConfig().getString("bossbar_style", "SOLID"));
        BossBar bossBar = Bukkit.createBossBar(bossBarText, bossBarColor, bossBarStyle);
        bossBar.addPlayer(event.getPlayer());
        bossBars.put(uuid, bossBar);
        UpdateBossBarPlaceholder updateTask = new UpdateBossBarPlaceholder(uuid, scheduledRemovalTime, bossBar, this);
        updateTask.startProgressUpdater();
        Bukkit.getScheduler().runTaskLater(plugin, new RemoveProtectedPlayerTask(uuid, protectedPlayers, scheduledRemovalTimes), Integer.parseInt(Objects.requireNonNull(getConfig().getString("protection_time_after_exiting_town_border"))));
    }

    @EventHandler
    public void on(PlayerEntersIntoTownBorderEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        System.out.println("Player just entered the town border");
        if (protectedPlayers.contains(uuid)) {
            protectedPlayers.remove(uuid);
            System.out.println("protectedPlayers after removing player: " + protectedPlayers);
            System.out.println("player protection ended");
            Long scheduledRemovalTime = scheduledRemovalTimes.get(uuid);
            if (scheduledRemovalTime == 0) {
                return;
            }
            scheduledRemovalTimes.remove(uuid);
            if (bossBars.containsKey(uuid)) {
                BossBar existingBossBar = bossBars.get(uuid);
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) {
                    return;
                }
                existingBossBar.removePlayer(player);
                bossBars.remove(uuid);
                // stop the running task if it exists
                if (bossBarTasks.containsKey(uuid)) {
                    Bukkit.getScheduler().cancelTask(bossBarTasks.get(uuid));
                    bossBarTasks.remove(uuid);
                }
            }
        }
    }
    public void remove(UUID uuid) {
    }
}
