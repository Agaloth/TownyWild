package com.agaloth.townywild.utils;

import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;
import org.bukkit.command.CommandSender;

import com.palmergames.bukkit.util.Colors;

public class Messaging {
    final static String prefix = Translation.of("plugin_prefix");

    public static void sendMsg(CommandSender sender, Translatable message) {
        if (sender != null)
            sender.sendMessage(prefix + Colors.White + message);
    }

    public static void sendErrorMsg(CommandSender sender, Translatable... messages) {
        if (sender != null)
            sender.sendMessage(prefix + Colors.Red + Translation.translateTranslatables(sender, messages));
    }
}
