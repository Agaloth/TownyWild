package com.agaloth.townywild;

import com.agaloth.townywild.commands.TownyWildAdminCommand;
import com.agaloth.townywild.commands.TownyWildToggleCommand;
import com.agaloth.townywild.hooks.TownyWildPlaceholderExpansion;
import com.agaloth.townywild.listeners.TownyWildSiegeWarEventListener;
import com.agaloth.townywild.listeners.TownyWildTownEventListener;
import com.agaloth.townywild.settings.Settings;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.util.Colors;
import com.palmergames.bukkit.util.Version;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class TownyWild extends JavaPlugin {

    public static TownyWild plugin;
    private static boolean siegeWar;
    private static final Version requiredTownyVersion = Version.fromString("0.98.4.0");
    private TownyWildPlaceholderExpansion expansion;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        ASCIIArt();

        // Registers the events from the TownyWildTownEventListener class.
        Bukkit.getServer().getPluginManager().registerEvents(new TownyWildTownEventListener(), this);

        // PlaceholderAPI Check
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                getLogger().info("Trying to load PAPI expansion...");
                this.expansion = new TownyWildPlaceholderExpansion();
                this.expansion.register();
                getLogger().info("Successfully registered the PAPI Expansion!");
            } catch (Exception e) {
                getLogger().severe("Failed to register the PAPI Expansion!");
                e.printStackTrace();
            }
        }

        // Towny Check
        try {
            if (townyVersionCheck(getTownyVersion())) {
                info("Towny version " + getTownyVersion() + " found.");
            } else {
                throw new RuntimeException("Towny version does not meet required minimum version: " + requiredTownyVersion);
            }
            if (Towny.getPlugin().isError()) {
                throw new RuntimeException("Towny is in error. TownyWild startup halted.");
            }
            Settings.loadConfig();
            Settings.loadLanguages();
            registerCommands();
            info("TownyWild has been Enabled.");
        } catch (Exception e) {
            severe(e.getMessage());
            e.printStackTrace();
            info("TownyWild did not start correctly.");
        }

        // Siege War Check
        if (getServer().getPluginManager().getPlugin("SiegeWar") != null) {
            Bukkit.getServer().getPluginManager().registerEvents(new TownyWildSiegeWarEventListener(this), this);
            getLogger().info("Successfully loaded the Siege War Expansion");
            siegeWar = true;
        }
    }


    // Towny Version Check
    private boolean townyVersionCheck(String version) {
        return Version.fromString(version).compareTo(requiredTownyVersion) >= 0;
    }

    // Registers the TownyWildAdmin command (used for reloading) and the TownyWildProtection command (used to toggle the protection)
    private void registerCommands() {
        Objects.requireNonNull(getCommand("townywildadmin")).setExecutor(new TownyWildAdminCommand());
        Objects.requireNonNull(getCommand("townywildprotection")).setExecutor(new TownyWildToggleCommand());
    }

    // Gets Towny's version
    private String getTownyVersion() {

        return Towny.getPlugin().getDescription().getVersion();
    }

    // Checks if Siege War is enabled on the server
    public static boolean siegeWarPresent() {
        return siegeWar;
    }

    public static TownyWild getPlugin() {

        return plugin;
    }

    public String getVersion() {

        return this.getDescription().getVersion();
    }

    public static void info(String message) {

        plugin.getLogger().info(message);
    }

    public static void severe(String message) {

        plugin.getLogger().severe(message);
    }

    private void ASCIIArt() {
            String art = System.lineSeparator() +
            ("                                       ") + System.lineSeparator() +
            (" _____                   _ _ _ _ _   _ ") + System.lineSeparator() +
            ("|_   _|___ _ _ _ ___ _ _| | | |_| |_| |") + System.lineSeparator() +
            ("  | | | . | | | |   | | | | | | | | . |") + System.lineSeparator() +
            ("  |_| |___|_____|_|_|_  |_____|_|_|___|") + System.lineSeparator() +
            ("                    |___|              ") + System.lineSeparator();

            Bukkit.getLogger().info(Colors.translateColorCodes(art));
        }

    @Override
    public void onDisable() {
        info("TownyWild has been Disabled.");
        if (expansion != null) {
            expansion.unregister();
        }
    }
}