package com.agaloth.townywild;

import com.agaloth.townywild.commands.TownyWildAdminCommand;
import com.agaloth.townywild.hooks.TownyWildPlaceholderExpansion;
import com.agaloth.townywild.listeners.TownyWildTownEventListener;
import com.agaloth.townywild.settings.Settings;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.util.Colors;
import com.palmergames.bukkit.util.Version;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TownyWild extends JavaPlugin {

    public static TownyWild plugin;
    private final TownyWildTownEventListener listener = new TownyWildTownEventListener(this);
    private static final Version requiredTownyVersion = Version.fromString("0.98.4.0");

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        ASCIIArt();

        // Registers the events from the TownyWildTownEventListener class.
        Bukkit.getServer().getPluginManager().registerEvents(new TownyWildTownEventListener(this), this);

        // PlaceholderAPI Check
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null){
            try{
                getLogger().info("Trying to load PAPI expansion...");
                new TownyWildPlaceholderExpansion().register();
                getLogger().info("Success!");
            } catch (Exception e){
                getLogger().severe("Failed!");
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
            if(Towny.getPlugin().isError()) {
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
    }

    // Towny Version Check
    private boolean townyVersionCheck(String version) {
        return Version.fromString(version).compareTo(requiredTownyVersion) >= 0;
    }

    // Registers the TownyWildAdmin command (used for reloading)
    private void registerCommands() {
        getCommand("townywildadmin").setExecutor(new TownyWildAdminCommand());
    }

    // Gets Towny's version
    private String getTownyVersion() {

        return Towny.getPlugin().getDescription().getVersion();
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
        // Plugin shutdown logic
        plugin = this;
        info("TownyWild has been Disabled.");
    }
}
