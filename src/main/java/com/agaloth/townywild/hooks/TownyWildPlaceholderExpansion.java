package com.agaloth.townywild.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TownyWildPlaceholderExpansion extends PlaceholderExpansion {
    public static Map<UUID, Long> protectionExpirationTime = new ConcurrentHashMap<>();

    public TownyWildPlaceholderExpansion() {
    }

    public long getRemainingProtectionTime(Player player) {

        // Takes the current time in milliseconds
        long now = System.currentTimeMillis();

        // Gets the expiration time from the protectionExpirationTime hashmap
        long expireTime = protectionExpirationTime.get(player.getUniqueId());

        // Then the placeholder returns a countdown with the following formula:
        return (expireTime - now) / 1000;
    }


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
