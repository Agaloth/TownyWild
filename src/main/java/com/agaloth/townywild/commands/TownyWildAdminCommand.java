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

import java.util.Collections;
import java.util.List;

public class TownyWildAdminCommand implements TabExecutor {

    private static final List<String> townyWildAdminTabCompletes = Collections.singletonList("reload");

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {

        // If the argument's length is equal to 1.
        if (args.length == 1)

            // Show "reload" as tab completion.
            return NameUtil.filterByStart(townyWildAdminTabCompletes, args[0]);

        else

            // Show nothing.
            return Collections.emptyList();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {

        // Execute the parseAdminCommand method when running the command.
        parseAdminCommand(sender, args);
        return true;
    }

    private void parseAdminCommand(CommandSender sender, String[] args) {

        // If the arguments is greater than 0.
        if (args.length > 0) {

            // If the sender is an instance of Player and if he doesn't have the permission to reload the plugin.
            if (sender instanceof Player && !sender.hasPermission(TownyWildPermissionNodes.TOWNYWILD_ADMIN_COMMAND.getNode(args[0]))) {

                // Send a message to the player telling him that he doesn't have permission.
                TownyMessaging.sendErrorMsg(sender, Translatable.of("plugin_prefix").append(Translatable.of("msg_err_command_disable")));
            return;
        }
            // If the first argument is "reload".
            if (args[0].equals("reload")) {

                // Run the parseReloadCommand method.
                parseReloadCommand(sender);

            } else {

                // Show the help message.
                showHelp(sender);
            }

} else {

    // If the sender is an instance of Player and if the sender doesn't have the permission to reload the plugin.
    if (sender instanceof Player && !sender.hasPermission(TownyWildPermissionNodes.TOWNYWILD_ADMIN_COMMAND.getNode())) {

    // Send a message to the player telling him that he doesn't have permission.
    TownyMessaging.sendErrorMsg(sender, Translatable.of("plugin_prefix").append(Translatable.of("msg_err_command_disable")));
    return;
        }
        // Show the help message.
        showHelp(sender);
        }
}

private void showHelp(CommandSender sender) {
    // Send a message to the player with /townywildadmin as the title.
    sender.sendMessage(ChatTools.formatTitle("/townywildadmin"));

    // Send a message to the player with more information about the command.
    sender.sendMessage(ChatTools.formatCommand("Eg", "/twa", "reload", Translatable.of("admin_help_reload").forLocale(sender)));
}

private void parseReloadCommand(CommandSender sender) {
        try {
        // Reload the config file.
        Settings.loadConfig();

        // Reload the Language file.
        Settings.loadLanguages();

        // Send a message to the player telling them that the config and language files have been reloaded.
        TownyMessaging.sendMessage(sender, Translatable.of("plugin_prefix").append(Translatable.of("config_and_lang_file_reloaded_successfully")));

        // If it catches an exception.
        } catch(Exception e) {

        // Send a message to the player telling them that the config and language files weren't able to be reloaded.
        TownyMessaging.sendErrorMsg(sender, Translatable.of("plugin_prefix").append(Translatable.of("config_and_lang_file_could_not_be_reloaded")));

        // Send a stack trace of the exception
        e.printStackTrace();
        }
    }
}
