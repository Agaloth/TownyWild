package com.agaloth.townywild.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.agaloth.townywild.TownyWild.plugin;
import static com.agaloth.townywild.settings.Settings.getConfig;
import static com.agaloth.townywild.tasks.UpdateBossBarProgress.uuid;

public class TownyWildPlaceholderExpansion extends PlaceholderExpansion {
    public static Map<UUID, Long> protectionExpirationTime = new HashMap<>();

    public TownyWildPlaceholderExpansion() {
    }

    public long getRemainingProtectionTime(Player player) {

        // Takes the current time in milliseconds
        long now = System.currentTimeMillis();

        // Gets the expiration time from the protectionExpirationTime hashmap
        long expireTime = protectionExpirationTime.get(uuid);

        // Then the placeholder returns a countdown with the following formula:
        return (expireTime - now) / 1000;
    }

    public long setRemainingProtectionTime(Player player) {

        // Takes the protection time from the config file.
        long configTime = (Integer.parseInt(Objects.requireNonNull(getConfig().getString("protection_time_after_exiting_town_border"))));

        // Uses a formula which takes the currentTimeMillis and adds the protection time from the config file multiplied by 1000.
        long expireTime = System.currentTimeMillis() + (configTime * 1000);

            // Creates a new Bukkit runTaskTimer runnable
            new BukkitRunnable() {

                @Override
                public void run() {
                    // If currentTimeMillis is higher or equal to the expireTime it will remove the player's protection.
                    if (System.currentTimeMillis() >= expireTime) {
                        protectionExpirationTime.remove(uuid, expireTime);
                    }

                }
            }.runTaskLater(plugin, configTime*20);
        return expireTime;
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

        // If the placeholder is %townywild_countdown%
        if (identifier.equals("countdown")) {

            long remainingProtectionTimeInSeconds = getRemainingProtectionTime((Player) player);

            // If the remaining protection time is greater than 60 seconds.
            if (remainingProtectionTimeInSeconds > 60) {

                // Convert it to minutes.
                long remainingProtectionTimeInMinutes = TimeUnit.SECONDS.toMinutes(remainingProtectionTimeInSeconds);

                // Return minutes instead of seconds (example: 1 minute).
                return Long.toString(remainingProtectionTimeInMinutes) + " minutes";

            } else {

                // Return seconds.
                return Long.toString(remainingProtectionTimeInSeconds) + " seconds";
            }
            }
            return "";
        }
}