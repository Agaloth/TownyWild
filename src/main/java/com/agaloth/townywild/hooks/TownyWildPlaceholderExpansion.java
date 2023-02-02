package com.agaloth.townywild.hooks;

import com.agaloth.townywild.listeners.TownyWildTownEventListener;
import com.google.common.cache.CacheBuilder;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.agaloth.townywild.settings.Settings.getConfig;

public class TownyWildPlaceholderExpansion extends PlaceholderExpansion {

    public TownyWildPlaceholderExpansion() {
    }
    Map<UUID, Integer> timeCounter = new HashMap<>();

    public long getRemainingProtectionTime(Player player) {
        if (TownyWildTownEventListener.protectionTimeLeft.containsKey(player.getUniqueId())) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(getPlugin());
            assert plugin != null;
            new BukkitRunnable() {
                int remainingTime = (Integer.parseInt(Objects.requireNonNull(getConfig().getString("protection_time_after_exiting_town_border"))));

                @Override
                public void run() {
                    timeCounter.put(player.getUniqueId(), remainingTime);
                    this.remainingTime--;
                    if (remainingTime < 0) {
                        cancel();
                        timeCounter.remove(player.getUniqueId());
                    }
                }
            }.runTaskTimer(plugin, 0, 20);
        }
            return timeCounter.getOrDefault(player.getUniqueId(), 0);
        }


    /**
     * This class will automatically register as a placeholder expansion
     * when a jar including this class is added to the directory
     * {@code /plugins/PlaceholderAPI/expansions} on your server.
     */


    /**
     * This method should always return true unless we
     * have a dependency we need to make sure is on the server
     * for our placeholders to work!
     *
     * @return always true since we do not have any dependencies.
     */
    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * The name of the person who created this expansion should go here
     */
    @Override
    public String getAuthor() {
        return "Agaloth";
    }

    /**
     * The placeholder identifier should go here
     * This is what tells PlaceholderAPI to call our onPlaceholderRequest method to obtain
     * a value if a placeholder starts with our identifier.
     * This must be unique and can not contain % or _
     */
    @Override
    public @NotNull String getIdentifier() {
        return "townywild";
    }

    /**
     * if an expansion requires another plugin as a dependency, the proper name of the dependency should
     * go here. Set this to null if your placeholders do not require another plugin be installed on the server
     * for them to work. This is extremely important to set if you do have a dependency because
     * if your dependency is not loaded when this hook is registered, it will be added to a cache to be
     * registered when plugin: "getPlugin()" is enabled on the server.
     */
    @Override
    public @NotNull String getPlugin() {
        return "TownyWild";
    }

    /**
     * This is the version of this expansion
     */
    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    /**
     * This is the method called when a placeholder with our identifier is found and needs a value
     * We specify the value identifier in this method
     */
    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (identifier.equals("countdown")) {
            long remainingProtectionTimeInSeconds = getRemainingProtectionTime((Player) player);
            if (remainingProtectionTimeInSeconds > 60) {
                long remainingProtectionTimeInMinutes = TimeUnit.SECONDS.toMinutes(remainingProtectionTimeInSeconds);
                return Long.toString(remainingProtectionTimeInMinutes) + " minutes";
            } else {
                return Long.toString(remainingProtectionTimeInSeconds) + " seconds";
            }
            }
            return "";
        }
}
