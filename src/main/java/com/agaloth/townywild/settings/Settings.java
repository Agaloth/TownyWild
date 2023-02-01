package com.agaloth.townywild.settings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.agaloth.townywild.TownyWild;
import com.palmergames.util.FileMgmt;
import org.bukkit.plugin.Plugin;

import com.palmergames.bukkit.config.CommentedConfiguration;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TranslationLoader;
import com.palmergames.util.TimeTools;

public class Settings {
    private static CommentedConfiguration config, newConfig = null;


    public static void loadConfig() throws Exception {
        TownyWild tw = TownyWild.getPlugin();
        try {
            Settings.loadConfig(tw.getDataFolder().getPath() + File.separator + "config.yml", tw.getVersion());
        } catch (Exception e) {
            TownyWild.severe("Config.yml failed to load! Disabling!");
            throw e;
        }
    }

    public static void loadLanguages() {
        try {
            Plugin plugin = TownyWild.getPlugin();
            Path langFolderPath = Paths.get(plugin.getDataFolder().getPath()).resolve("lang");
            TranslationLoader loader = new TranslationLoader(langFolderPath, plugin, TownyWild.class);
            loader.load();
            TownyAPI.getInstance().addTranslations(plugin, loader.getTranslations());
        } catch (Exception e) {
            TownyWild.severe("Language file failed to load! Disabling!");
            throw e;
        }
    }

    private static void loadConfig(String filepath, String version) throws Exception {
        if (FileMgmt.checkOrCreateFile(filepath)) {
            File file = new File(filepath);
            config = new CommentedConfiguration(file.toPath());
            if (!config.load())
                throw new IOException("Failed to load config!");

            setDefaults(version, file);
            config.save();
        }
    }

    public static void addComment(String root, String... comments) {
        newConfig.addComment(root.toLowerCase(), comments);
    }

    private static void setNewProperty(String root, Object value) {

        if (value == null) {
            value = "";
        }
        newConfig.set(root.toLowerCase(), value.toString());
    }

    private static void setProperty(String root, Object value) {
        config.set(root.toLowerCase(), value.toString());
    }

    private static void setDefaults(String version, File file) {
        newConfig = new CommentedConfiguration(file.toPath());
        newConfig.load();

        for (ConfigNodes root : ConfigNodes.values()) {
            if (root.getComments().length > 0)
                addComment(root.getRoot(), root.getComments());
            if (root.getRoot() == ConfigNodes.VERSION.getRoot())
                setNewProperty(root.getRoot(), version);
            else
                setNewProperty(root.getRoot(), (config.get(root.getRoot().toLowerCase()) != null) ? config.get(root.getRoot().toLowerCase()) : root.getDefault());
        }

        config = newConfig;
                newConfig = null;
    }

    public static String getString(String root, String def) {
        String data = config.getString(root.toLowerCase(), def);
        if (data == null) {
            sendError(root.toLowerCase() + " from config.yml");
            return "";
        }
        return data;
    }

    private static void sendError(String msg) {
        TownyWild.severe("Error could not read " + msg);
    }

    public static boolean getBoolean(ConfigNodes node) {
        return Boolean.parseBoolean(config.getString(node.getRoot().toLowerCase(), node.getDefault()));
    }

    public static double getDouble(ConfigNodes node) {
        try {
        return Double.parseDouble(config.getString(node.getRoot().toLowerCase(), node.getDefault()).trim());
    } catch (NumberFormatException e) {
        sendError(node.getRoot().toLowerCase() + " from config.yml");
        return 0.0;
    }
}

public static int getInt(ConfigNodes node) {
    try {
        return Integer.parseInt(config.getString(node.getRoot().toLowerCase(), node.getDefault()).trim());
    } catch (NumberFormatException e) {
        sendError(node.getRoot().toLowerCase() + " from config.yml");
        return 0;
    }
}

public static String getString(ConfigNodes node) {
    return config.getString(node.getRoot().toLowerCase(), node.getDefault());
    }
    public static long getSeconds(ConfigNodes node) {
    try {
        return TimeTools.getSeconds(getString(node));
    } catch (NumberFormatException e) {
        sendError(node.getRoot().toLowerCase() + " from config.yml");
        return 1;
    }
}

public static CommentedConfiguration getConfig() {
    return config;
}
}