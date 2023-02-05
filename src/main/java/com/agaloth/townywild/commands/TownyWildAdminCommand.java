package com.agaloth.townywild.commands;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.util.ChatTools;

import com.agaloth.townywild.enums.TownyWildPermissionNodes;
import com.agaloth.townywild.settings.Settings;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TownyWildAdminCommand implements TabExecutor {

    private static final List<String> townyWildAdminTabCompletes = Collections.singletonList("reload");

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1)
            return NameUtil.filterByStart(townyWildAdminTabCompletes, args[0]);
        else
            return Collections.emptyList();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {
        parseAdminCommand(sender, args);
        return true;
    }

    private void parseAdminCommand(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (sender instanceof Player && !sender.hasPermission(TownyWildPermissionNodes.TOWNYWILD_ADMIN_COMMAND.getNode(args[0]))) {
                TownyMessaging.sendErrorMsg(sender, Translatable.of("plugin_prefix").append(Translatable.of("msg_err_command_disable")));
            return;
        }
            if (args[0].equals("reload")) {
                parseReloadCommand(sender);
            } else {
                showHelp(sender);
            }
} else {
    if (sender instanceof Player
            && !sender.hasPermission(TownyWildPermissionNodes.TOWNYWILD_ADMIN_COMMAND.getNode())) {
    TownyMessaging.sendErrorMsg(sender, Translatable.of("plugin_prefix").append(Translatable.of("msg_err_command_disable")));
    return;
        }
        showHelp(sender);
        }
}

private void showHelp(CommandSender sender) {
    sender.sendMessage(ChatTools.formatTitle("/townywildadmin"));
    sender.sendMessage(ChatTools.formatCommand("Eg", "/twa", "reload", Translatable.of("admin_help_reload").forLocale(sender)));
}

private void parseReloadCommand(CommandSender sender) {
        try {
        Settings.loadConfig();
        Settings.loadLanguages();
        TownyMessaging.sendMessage(sender, Translatable.of("plugin_prefix").append(Translatable.of("config_and_lang_file_reloaded_successfully")));
        } catch(Exception e) {
        TownyMessaging.sendErrorMsg(sender, Translatable.of("plugin_prefix").append(Translatable.of("config_and_lang_file_could_not_be_reloaded")));
        e.printStackTrace();
        }
    }
}
