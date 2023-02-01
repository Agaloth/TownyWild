package com.agaloth.townywild.utils;

import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.util.Colors;

import com.agaloth.townywild.TownyWild;

import java.util.List;

public class Messaging {
    final static String prefix = Translation.of("plugin_prefix");

    public static void sendErrorMsg(CommandSender sender, String message) {
        if(sender != null)
            sender.sendMessage(prefix + Colors.Red + message);
    }

    public static void sendMsg(CommandSender sender, Translatable message) {
        if (sender != null)
            sender.sendMessage(prefix + Colors.White + message);
    }

    public static void sendGlobalMessage(String message) {
        TownyWild.info(message);
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p != null)
                .filter(p -> TownyAPI.getInstance().isTownyWorld(p.getLocation().getWorld()))
                .forEach(p -> sendMsg(p, Translatable.of(message)));
    }

    public static void sendGlobalMessage(String header, List<String> lines) {
        TownyWild.info(header);
        for(String line: lines) {
            TownyWild.info(line);
        }
        for(Player player: Bukkit.getOnlinePlayers()) {
            if(player != null && TownyAPI.getInstance().isTownyWorld(player.getLocation().getWorld())) {
                player.sendMessage(prefix + header);
                for(String line: lines) {
                    player.sendMessage(line);
                }
                }
            }
        }

        public static void sendErrorMsg(CommandSender sender, Translatable... messages) {
        if (sender != null)
            sender.sendMessage(prefix + Colors.Red + Translation.translateTranslatables(sender, messages));
    }

    public static void SendMsg(CommandSender sender, Translatable... messages) {
        if (sender != null)
            sender.sendMessage(prefix + Colors.White + Translation.translateTranslatables(sender, messages));
    }

    public static void sendGlobalMessage(Translatable message) {
        TownyWild.info(message.defaultLocale());
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p != null)
                .filter(p -> TownyAPI.getInstance().isTownyWorld(p.getLocation().getWorld()))
                .forEach(p -> sendMsg(p, message));
    }

    public static void sendGlobalMessage(Translatable header, List<Translatable> lines) {
        TownyWild.info(header.defaultLocale());
        for(Translatable line: lines) {
            TownyWild.info(line.defaultLocale());
        }
        for(Player player: Bukkit.getOnlinePlayers()) {
            if(player != null && TownyAPI.getInstance().isTownyWorld(player.getLocation().getWorld())) {
                player.sendMessage(prefix + header.forLocale(player));
                for(Translatable line: lines) {
                    sendMsg(player, line);
                }
                }
            }
        }

        public static void sendGlobalMessage(Translatable[] lines) {
        for(Translatable line: lines) {
            TownyWild.info(line.defaultLocale());
        }
        for(Player player: Bukkit.getOnlinePlayers()) {
            if(player != null && TownyAPI.getInstance().isTownyWorld(player.getLocation().getWorld())) {
                for(Translatable line: lines) {
                    sendMsg(player, line);
                }
            }
        }
    }
}
