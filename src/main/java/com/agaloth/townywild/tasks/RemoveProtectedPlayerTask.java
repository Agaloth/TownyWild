package com.agaloth.townywild.tasks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

import static com.agaloth.townywild.listeners.TownyWildTownEventListener.protectedPlayers;
import static com.agaloth.townywild.listeners.TownyWildTownEventListener.protectionTimeLeft;

public class RemoveProtectedPlayerTask implements Runnable {
    private final UUID uuid;

    public RemoveProtectedPlayerTask(UUID uuid) {

        this.uuid = uuid;
    }

    public void protectionTimeLeft() {
        if (protectionTimeLeft.containsKey(uuid)) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(this.toString());
            assert plugin != null;
            new BukkitRunnable() {

                int remainingTime = 15;

                public void updateProtectionTime() {
                    protectedPlayers.add(uuid);
                    this.remainingTime--;
                    if (remainingTime < 0) {
                        protectedPlayers.remove(uuid);
                        cancel();
                    }
                }
                @Override
                public void run() {
                    updateProtectionTime();
                }
            }.runTaskTimer(plugin, 20, 20);
        }
        protectionTimeLeft.getOrDefault(uuid, 0);
    }

    @Override
    public void run() {
        protectionTimeLeft();
    }
}
